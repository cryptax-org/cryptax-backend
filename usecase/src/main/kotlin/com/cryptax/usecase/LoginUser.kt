package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.NotAllowedException
import com.cryptax.domain.port.PasswordEncoder
import com.cryptax.domain.port.UserRepository

class LoginUser(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

	fun login(email: String, password: String): User {
		val user = userRepository.findByEmail(email).orElseThrow { NotAllowedException("Not allowed") }
		val hashedPassword = passwordEncoder.encode(email + password)
		if (user.password != hashedPassword) throw NotAllowedException("Not allowed")
		return user
	}
}
