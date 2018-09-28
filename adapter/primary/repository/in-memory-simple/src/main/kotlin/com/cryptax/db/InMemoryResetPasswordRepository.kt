package com.cryptax.db

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.port.ResetPasswordRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InMemoryResetPasswordRepository : ResetPasswordRepository {

    private val inMemoryDb = HashMap<String, ResetPassword>()

    override fun save(resetPassword: ResetPassword): Single<ResetPassword> {
        return Single.fromCallable {
            log.debug("Create ResetPassword $resetPassword")
            inMemoryDb[resetPassword.userId] = resetPassword
            resetPassword
        }
    }

    override fun findByUserId(userId: String): Maybe<ResetPassword> {
        return Maybe.defer {
            log.debug("Get ResetPassword by userId [$userId]")
            val resetPassword = inMemoryDb[userId]
            when (resetPassword) {
                null -> Maybe.empty<ResetPassword>()
                else -> Maybe.just(resetPassword)
            }
        }
    }

    override fun delete(userId: String): Single<Unit> {
        return Single.fromCallable {
            log.debug("Delete ResetPassword by userId [$userId]")
            inMemoryDb.remove(userId)
            Unit
        }
    }

    override fun ping(): Boolean {
        return true
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(InMemoryUserRepository::class.java)
    }
}
