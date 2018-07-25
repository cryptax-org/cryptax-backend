package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateCreateUser
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
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
        validateCreateUser(user)

        val userToSave = User(
            id = idGenerator.generate(),
            email = user.email,
            password = securePassword.securePassword(user.password).toCharArray(),
            lastName = user.lastName,
            firstName = user.firstName,
            allowed = false
        )
        val welcomeToken = securePassword.generateToken(userToSave)
        Arrays.fill(user.password, '\u0000')

        return repository
            .findByEmail(user.email)
            .subscribeOn(Schedulers.io())
            .isEmpty
            .map { isEmpty ->
                when (isEmpty) {
                    true -> repository.create(userToSave)
                    false -> throw UserAlreadyExistsException(user.email)
                }
            }
            .blockingGet()
            .zipWith(Single.just(welcomeToken), BiFunction { u: User, t: String -> Pair(u, t) })
            .doOnSuccess {
                emailService.welcomeEmail(userToSave, welcomeToken)
            }
            .onErrorResumeNext { t: Throwable -> Single.error(t) }
    }
}
