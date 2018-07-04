package com.cryptax.domain.port

import com.cryptax.domain.entity.User
import java.util.Optional

interface UserRepository {

    fun create(user: User): User

    fun findById(id: String): Optional<User>

    fun findByEmail(email: String): Optional<User>

    fun findAllUsers(): List<User>
}
