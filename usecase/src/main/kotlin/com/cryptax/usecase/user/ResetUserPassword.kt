package com.cryptax.usecase.user

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.ResetPasswordException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Arrays

class ResetUserPassword(
    private val idGenerator: IdGenerator,
    private val securePassword: SecurePassword,
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val resetPasswordRepository: ResetPasswordRepository) {

    /**
     * First step during password reset. Get the user, create a token, save it in the DB, and send the email
     */
    fun initiatePasswordReset(email: String): Single<ResetPassword> {
        log.info("Initiate reset password for $email")
        return userRepository.findByEmail(email)
            .toSingle()
            .map { user ->
                ResetPassword(
                    user.id,
                    idGenerator.generate(),
                    ZonedDateTime.now(ZoneId.of("UTC")))
            }
            .flatMap { resetPassword -> resetPasswordRepository.save(resetPassword) }
            .doAfterSuccess { resetPassword -> emailService.resetPasswordEmail(email, resetPassword) }
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(UserNotFoundException(email))
                    else -> Single.error(throwable)
                }
            }
    }

    fun resetPassword(email: String, password: CharArray, token: String): Single<Unit> {
        log.info("Reset password for $email")
        return userRepository.findByEmail(email)
            .toSingle()
            .flatMap { user ->
                resetPasswordRepository.findByUserId(user.id)
                    .toSingle()
                    .map { resetPassword ->
                        val nowMinusOneDay = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1)
                        resetPassword.token == token && resetPassword.date.isAfter(nowMinusOneDay)
                    }
                    .flatMap { isValid ->
                        if (!isValid) throw ResetPasswordException(email)
                        val result = userRepository.updateUser(User(
                            id = user.id,
                            email = user.email,
                            password = securePassword.securePassword(password).toCharArray(),
                            lastName = user.lastName,
                            firstName = user.firstName,
                            allowed = user.allowed))
                        Arrays.fill(user.password, '\u0000')
                        result
                    }
                    .flatMap { u -> resetPasswordRepository.delete(u.id) }
            }
            .doAfterSuccess { _ -> emailService.resetPasswordConfirmationEmail(email) }
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(ResetPasswordException(email))
                    else -> Single.error(throwable)
                }
            }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ResetUserPassword::class.java)
    }
}
