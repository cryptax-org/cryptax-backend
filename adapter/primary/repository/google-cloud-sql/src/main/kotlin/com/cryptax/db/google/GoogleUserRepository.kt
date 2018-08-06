package com.cryptax.db.google

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.jooq.Record
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
        private val idField = field(name("id"), SQLDataType.VARCHAR)
        private val emailField = field(name("email"), SQLDataType.VARCHAR)
        private val passwordField = field(name("password"), SQLDataType.VARCHAR)
        private val lastNameField = field(name("lastName"), SQLDataType.VARCHAR)
        private val firstNameField = field(name("firstName"), SQLDataType.VARCHAR)
        private val allowedField = field(name("allowed"), SQLDataType.BOOLEAN)
    }

    init {
        DSL.using(connection, dialect)
            .createTableIfNotExists(userTable)
            .columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)
            .execute()
    }

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Create a user $user")
            DSL.using(connection, dialect)
                .insertInto(userTable)
                .columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)
                .values(user.id, user.email, user.password.joinToString(separator = ""), user.lastName, user.firstName, user.allowed)
                .execute()
            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            val record = DSL.using(connection, dialect)
                .selectFrom(userTable)
                .where(idField.eq(id))
                .fetchOne()
            when (record) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toUser(record))
            }
        }
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            val record = DSL.using(connection, dialect)
                .selectFrom(userTable)
                .where(emailField.eq(email))
                .fetchOne()
            when (record) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toUser(record))
            }
        }
    }

    override fun updateUser(user: User): User {
        throw RuntimeException("Not implemented yet")
    }

    override fun ping(): Boolean {
        val record = DSL.using(connection, dialect).resultQuery("SELECT 1").fetch()
        return record.isNotEmpty
    }

    private fun toUser(record: Record): User {
        return User(
            record.get(idField, String::class.java),
            record.get(emailField, String::class.java),
            record.get(passwordField, String::class.java).toCharArray(),
            record.get(lastNameField, String::class.java),
            record.get(firstNameField, String::class.java),
            record.get(allowedField, Boolean::class.java))
    }
}
