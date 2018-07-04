package com.cryptax.domain.port

import com.cryptax.domain.entity.User

interface UserRepository {

	fun create(user: User): User

	fun findById(id: String): User?

	fun findByEmail(email: String): User?

	fun findAllUsers(): List<User>
}
