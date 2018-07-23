package com.cryptax.db

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single

class InMemoryUserRepository : UserRepository {

    private val inMemoryDb = HashMap<String, User>()

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            println("DB create " + Thread.currentThread().name)
            inMemoryDb[user.id!!] = user
            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): User? {
        return inMemoryDb[id]
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            println("FInd by email " + Thread.currentThread().name)
            val user = inMemoryDb.values.firstOrNull { user -> user.email == email }
            when (user) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(user)
            }
        }
    }

    override fun updateUser(user: User): User {
        inMemoryDb[user.id!!] = user
        return user
    }
}
