package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class ValidateUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

    companion object {
        private val log = LoggerFactory.getLogger(CreateUser::class.java)
    }

    fun validate(userId: String, welcomeToken: String): Single<Boolean> {
        log.info("Validate user $userId with token $welcomeToken")
        return userRepository.findById(userId)
            .subscribeOn(Schedulers.io())
            .filter { user -> securePassword.generateToken(user) == welcomeToken }
            .map { user -> User(id = user.id, email = user.email, firstName = user.firstName, lastName = user.lastName, password = user.password, allowed = true) }
            .map { user: User -> userRepository.updateUser(user).flatMap { Single.just(true) } }
            .flatMapSingle { it }
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(UserNotFoundException(userId))
                    else -> Single.error(throwable)
                }
            }
    }
}
