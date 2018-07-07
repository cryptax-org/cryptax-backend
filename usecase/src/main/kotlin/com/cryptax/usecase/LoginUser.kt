package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.NotAllowedException
import com.cryptax.domain.port.PasswordEncoder
import com.cryptax.domain.port.UserRepository

class LoginUser(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

	fun login(email: String, password: CharArray): User {
		val user = userRepository.findByEmail(email)
		if (user == null) {
			throw NotAllowedException("Not allowed")
		} else {
			val hashedPassword = passwordEncoder.encode(email + password.joinToString(separator = ""))
			if (user.password.joinToString(separator = "") != hashedPassword) throw NotAllowedException("Not allowed")
			return user
		}
	}
}
