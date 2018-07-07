package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository

class FindUser(private val repository: UserRepository) {

	fun findById(id: String): User? {
		return repository.findById(id)
	}

	fun findAllUsers(): List<User> {
		return repository.findAllUsers()
	}
}
