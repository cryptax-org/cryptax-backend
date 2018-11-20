package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class LoginUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

    fun login(email: String, password: CharArray): Single<User> {
        return userRepository
            .findByEmail(email)
            .observeOn(Schedulers.computation())
            .flatMapSingle { user ->
                if (!securePassword.matchPassword(password, user.password))
                    throw LoginException(email, "Password do not match")
                if (!user.allowed) {
                    throw LoginException(email, "Not allowed to login")
                }
                Single.just(user)
            }
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(LoginException(email, "User not found"))
                    else -> Single.error(throwable)
                }
            }
    }
}
