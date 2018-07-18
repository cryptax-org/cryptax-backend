package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single

class LoginUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

    fun login(email: String, password: CharArray): Single<User> {
/*        val user = userRepository.findByEmail(email) ?: throw LoginException(email, "User not found")
        if (!securePassword.matchPassword(password, user.password))
            throw LoginException(email, "Password do not match")
        if (!user.allowed) {
            throw LoginException(email, "Not allowed to login")
        }
        return user*/
        val maybe: Maybe<User> = userRepository.findByEmail(email)


        return maybe
            .filter { user ->
                if (!securePassword.matchPassword(password, user.password))
                    throw LoginException(email, "Password do not match")
                if (!user.allowed) {
                    throw LoginException(email, "Not allowed to login")
                }
                true
            }
            .toSingle()
    }
}
