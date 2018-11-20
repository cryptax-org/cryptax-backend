package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.port.ResetPasswordRepository
import com.google.cloud.Timestamp
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.Query
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneId

class CloudDatastoreResetPasswordRepository(datastore: Datastore) : ResetPasswordRepository, CloudDatastore(datastore) {

    override fun save(resetPassword: ResetPassword): Single<ResetPassword> {
        return Single.fromCallable<ResetPassword> {
            log.debug("Create ResetPassword $resetPassword")
            datastore.put(toEntity(resetPassword))
            resetPassword
        }
            .subscribeOn(Schedulers.io())
    }

    override fun findByUserId(userId: String): Maybe<ResetPassword> {
        return Maybe.defer {
            log.debug("Get ResetPassword by userId [$userId]")
            val entity = datastore.get(key(userId))
            when (entity) {
                null -> Maybe.empty<ResetPassword>()
                else -> Maybe.just(toResetPassword(entity))
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun delete(userId: String): Single<Unit> {
        return Single.fromCallable {
            log.debug("Delete ResetPassword $userId")
            datastore.delete(key(userId))
        }
            .subscribeOn(Schedulers.io())
    }

    override fun ping(): Boolean {
        log.debug("Ping reset password repository")
        return try {
            datastore.run(Query.newGqlQueryBuilder("SELECT token FROM $kind LIMIT 1").setAllowLiteral(true).build())
            true
        } catch (e: DatastoreException) {
            log.error("Could not ping Google Cloud", e)
            false
        }
    }

    private fun key(id: String): Key {
        return datastore.newKeyFactory().setKind(kind).newKey(id)
    }

    private fun toEntity(resetPassword: ResetPassword): Entity {
        return Entity.newBuilder(datastore.newKeyFactory().setKind(kind).newKey(resetPassword.userId))
            .set("token", resetPassword.token)
            .set("date", Timestamp.ofTimeSecondsAndNanos(resetPassword.date.toInstant().epochSecond, resetPassword.date.toInstant().nano))
            .build()
    }

    private fun toResetPassword(entity: Entity): ResetPassword {
        val date = entity.getTimestamp("date").toDate()
        return ResetPassword(
            userId = entity.key.name,
            token = entity.getString("token"),
            date = date.toInstant().atZone(zoneId))
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudDatastoreResetPasswordRepository::class.java)
        private val zoneId = ZoneId.of("UTC")
        private val kind = ResetPassword::class.java.simpleName
    }
}
