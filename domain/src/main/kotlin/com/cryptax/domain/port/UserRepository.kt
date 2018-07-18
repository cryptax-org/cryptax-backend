package com.cryptax.domain.port

import com.cryptax.domain.entity.User
import io.reactivex.Maybe
import io.reactivex.Single

interface UserRepository {

    fun create(user: User): Single<User>

    fun findById(id: String): User?

    fun findByEmail(email: String): Maybe<User>

    fun updateUser(user: User): User
}
