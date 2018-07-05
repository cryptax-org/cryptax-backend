package com.cryptax.db

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository

class InMemoryUserRepository : UserRepository {

	private val inMemoryDb = HashMap<String, User>()

	override fun create(user: User): User {
		inMemoryDb[user.id!!] = user
		return user
	}

	override fun findById(id: String): User? {
		return inMemoryDb[id]
	}

	override fun findByEmail(email: String): User? {
		return inMemoryDb.values.firstOrNull { user -> user.email == email }
	}

	override fun findAllUsers(): List<User> {
		return ArrayList(inMemoryDb.values)
	}
}
