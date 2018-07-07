package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.NotAllowedException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository

class LoginUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

	fun login(email: String, password: CharArray): User {
		val user = userRepository.findByEmail(email)
		if (user == null) {
			throw NotAllowedException("Not allowed")
		} else {
			if (!securePassword.matchPassword(password, user.password))
				throw NotAllowedException("Not allowed")
			return user
		}
	}
}
