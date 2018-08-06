package com.cryptax.db.google

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection

class GoogleUserRepository(private val connection: Connection) : UserRepository {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GoogleUserRepository::class.java)
        private val dialect = SQLDialect.POSTGRES
        private val userTable = table(name("user"))
        private val id = field(name("id"), SQLDataType.VARCHAR)
        private val email = field(name("email"), SQLDataType.VARCHAR)
        private val password = field(name("password"), SQLDataType.VARCHAR)
        private val lastName = field(name("lastName"), SQLDataType.VARCHAR)
        private val firstName = field(name("firstName"), SQLDataType.VARCHAR)
        private val allowed = field(name("allowed"), SQLDataType.BOOLEAN)
    }

    init {
        DSL.using(connection, dialect)
            .createTableIfNotExists(userTable)
            .columns(id, email, password, lastName, firstName, allowed)
            .execute()
    }

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Create a user $user")
            DSL.using(connection, dialect)
                .insertInto(userTable)
                .columns(id, email, password, lastName, firstName, allowed)
                .values(user.id, user.email, user.password.joinToString(separator = ""), user.lastName, user.firstName, user.allowed)
                .execute()

            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            emitter.onError(RuntimeException("Not implemented yet"))
        }
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            val record = DSL.using(connection, dialect)
                .selectFrom(userTable)
                .where(field("email").eq(email))
                .fetchOne()
            when (record) {
                null -> emitter.onComplete()
                else -> {
                    val password = record.get(password, String::class.java)
                    val user = User(
                        record.get(id, String::class.java),
                        record.get(GoogleUserRepository.email, String::class.java),
                        password.toCharArray(),
                        record.get(lastName, String::class.java),
                        record.get(firstName, String::class.java),
                        record.get(allowed, Boolean::class.java))
                    emitter.onSuccess(user)
                }
            }
        }
    }

    override fun updateUser(user: User): User {
        throw RuntimeException("Not implemented yet")
    }

    override fun ping(): Boolean {
        return false
    }
}
