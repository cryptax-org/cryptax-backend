package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository

class LoginUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

	fun login(email: String, password: CharArray): User {
		val user = userRepository.findByEmail(email) ?: throw LoginException(email, "User not found")
		if (!securePassword.matchPassword(password, user.password))
			throw LoginException(email, "Password do not match")
		return user
	}
}
