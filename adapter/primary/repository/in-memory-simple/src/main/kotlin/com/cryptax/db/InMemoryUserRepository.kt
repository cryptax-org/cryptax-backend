package com.cryptax.db

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(InMemoryUserRepository::class.java)

class InMemoryUserRepository : UserRepository {

    private val inMemoryDb = HashMap<String, User>()

    override fun create(user: User): Single<User> {
        return Single.fromCallable {
            log.debug("Create a user $user")
            inMemoryDb[user.id] = user
            user
        }
            .subscribeOn(Schedulers.io())
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.defer {
            log.debug("Find user by id [$id]")
            val user = inMemoryDb[id]
            when (user) {
                null -> Maybe.empty<User>()
                else -> Maybe.just(user)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.defer {
            log.debug("Find user by email [$email]")
            val user = inMemoryDb.values.firstOrNull { user -> user.email == email }
            when (user) {
                null -> Maybe.empty<User>()
                else -> Maybe.just(user)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun updateUser(user: User): Single<User> {
        return Single.fromCallable {
            log.debug("Update user $user")
            inMemoryDb[user.id] = user
            user
        }
            .subscribeOn(Schedulers.io())
    }

    fun deleteAll() {
        inMemoryDb.clear()
    }

    override fun ping(): Boolean {
        return true
    }
}
