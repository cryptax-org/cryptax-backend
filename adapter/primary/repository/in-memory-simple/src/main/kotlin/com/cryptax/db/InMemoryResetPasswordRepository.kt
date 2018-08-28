package com.cryptax.db

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.port.ResetPasswordRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InMemoryResetPasswordRepository : ResetPasswordRepository {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(InMemoryUserRepository::class.java)
    }

    private val inMemoryDb = HashMap<String, ResetPassword>()

    override fun save(resetPassword: ResetPassword): Single<ResetPassword> {
        return Single.create<ResetPassword> { emitter ->
            log.debug("Create ResetPassword $resetPassword")
            inMemoryDb[resetPassword.userId] = resetPassword
            emitter.onSuccess(resetPassword)
        }
    }

    override fun findByUserId(userId: String): Maybe<ResetPassword> {
        return Maybe.create<ResetPassword> { emitter ->
            log.debug("Get ResetPassword by userId [$userId]")
            val resetPassword = inMemoryDb[userId]
            when (resetPassword) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(resetPassword)
            }
        }
    }

    override fun delete(userId: String): Single<Unit> {
        return Single.create<Unit> { emitter ->
            log.debug("Delete ResetPassword by userId [$userId]")
            inMemoryDb.remove(userId)
            emitter.onSuccess(Unit)
        }
    }

    override fun ping(): Boolean {
        return true
    }
}
