package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateCreateUser
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Arrays

private val log: Logger = LoggerFactory.getLogger(CreateUser::class.java)

class CreateUser(
    private val repository: UserRepository,
    private val securePassword: SecurePassword,
    private val idGenerator: IdGenerator,
    private val emailService: EmailService) {

    fun create(user: User): Single<Pair<User, String>> {
        log.info("Usecase, create a user $user")
        return validateCreateUser(user)
            .flatMap { repository.findByEmail(user.email).isEmpty }
            .map { isEmpty ->
                when (isEmpty) {
                    true -> {
                        val u = User(
                            id = idGenerator.generate(),
                            email = user.email,
                            password = securePassword.securePassword(user.password).toCharArray(),
                            lastName = user.lastName,
                            firstName = user.firstName,
                            allowed = false)
                        Arrays.fill(user.password, '\u0000')
                        u
                    }
                    false -> throw UserAlreadyExistsException(user.email)
                }
            }
            .flatMap { u -> repository.create(u) }
            .map { u -> Pair(u, securePassword.generateToken(u)) }
            .doOnSuccess { pair -> emailService.welcomeEmail(pair.first, pair.second) }
            .onErrorResumeNext { t: Throwable -> Single.error(t) }
    }
}
