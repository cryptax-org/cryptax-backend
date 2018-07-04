package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import java.util.*

class FindUser(private val repository: UserRepository) {

	fun findById(id: String): Optional<User> {
		return repository.findById(id)
	}

	fun findAllUsers(): List<User> {
		return repository.findAllUsers()
	}
}
