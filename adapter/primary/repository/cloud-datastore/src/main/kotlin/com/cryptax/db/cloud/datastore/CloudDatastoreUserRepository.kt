package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StructuredQuery
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudDatastoreUserRepository(datastore: Datastore) : UserRepository, CloudDatastore(datastore) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudDatastoreUserRepository::class.java)
        private val kind = User::class.java.simpleName
    }

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Create a user $user")
            datastore.put(toEntity(user))
            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            log.debug("Get a user by id [$id]")
            val record = datastore.get(datastore.newKeyFactory().setKind(kind).newKey(id))
            when (record) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toUser(record))
            }
        }
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            val query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(StructuredQuery.PropertyFilter.eq("email", email))
                .build()
            val queryResults: QueryResults<Entity> = datastore.run(query)
            when (queryResults.hasNext()) {
                false -> emitter.onComplete()
                true -> emitter.onSuccess(toUser(queryResults.next()))
            }
        }
    }

    override fun updateUser(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Update a user $user")
            datastore.update(toEntity(user))
            emitter.onSuccess(user)
        }
    }

    override fun ping(): Boolean {
        return try {
            datastore.run(Query.newGqlQueryBuilder("SELECT email FROM $kind LIMIT 1").setAllowLiteral(true).build())
            true
        } catch (e: DatastoreException) {
            log.error("Could not ping Google Cloud", e)
            false
        }
    }

    private fun toEntity(user: User): Entity {
        return Entity.newBuilder(datastore.newKeyFactory().setKind(kind).newKey(user.id))
            .set("email", user.email)
            .set("password", user.password.joinToString(separator = ""))
            .set("lastName", user.lastName)
            .set("firstName", user.firstName)
            .set("allowed", user.allowed)
            .build()
    }

    private fun toUser(entity: Entity): User {
        return User(
            id = entity.key.name,
            email = entity.getString("email"),
            password = entity.getString("password").toCharArray(),
            lastName = entity.getString("lastName"),
            firstName = entity.getString("firstName"),
            allowed = entity.getBoolean("allowed")
        )
    }
}
