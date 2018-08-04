package com.cryptax.db

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(InMemoryUserRepository::class.java)

class InMemoryUserRepository : UserRepository {

    private val inMemoryDb = HashMap<String, User>()

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Create a user $user")
            inMemoryDb[user.id] = user
            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            log.debug("Find user by id [$id]")
            val user = inMemoryDb[id]
            when (user) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(user)
            }
        }
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            log.debug("Find user by email [$email]")
            val user = inMemoryDb.values.firstOrNull { user -> user.email == email }
            when (user) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(user)
            }
        }
    }

    override fun updateUser(user: User): User {
        inMemoryDb[user.id] = user
        return user
    }

    override fun ping(): Boolean {
        return true
    }
}
