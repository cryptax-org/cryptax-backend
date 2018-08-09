package com.cryptax.db.google

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GoogleUserRepository(private val dslContext: DSLContext) : UserRepository {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GoogleUserRepository::class.java)
        val tableUser: Table<Record> = table(name("user"))
        val idField: Field<String> = field(name("id"), SQLDataType.VARCHAR)
        val emailField: Field<String> = field(name("email"), SQLDataType.VARCHAR.nullable(false))
        val passwordField: Field<String> = field(name("password"), SQLDataType.VARCHAR.nullable(false))
        val lastNameField: Field<String> = field(name("lastName"), SQLDataType.VARCHAR.nullable(false))
        val firstNameField: Field<String> = field(name("firstName"), SQLDataType.VARCHAR.nullable(false))
        val allowedField: Field<Boolean> = field(name("allowed"), SQLDataType.BOOLEAN.nullable(false))
    }

    init {
        dslContext
            .createTableIfNotExists(tableUser)
            .columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)
            .constraints(constraint("PK_USER").primaryKey(idField))
            .execute()
    }

    override fun create(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Create a user $user")
            dslContext
                .insertInto(tableUser)
                .columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)
                .values(user.id, user.email, user.password.joinToString(separator = ""), user.lastName, user.firstName, user.allowed)
                .execute()
            emitter.onSuccess(user)
        }
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.create<User> { emitter ->
            val record = dslContext
                .selectFrom(tableUser)
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
            val record = dslContext
                .selectFrom(tableUser)
                .where(emailField.eq(email))
                .fetchOne()
            when (record) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toUser(record))
            }
        }
    }

    override fun updateUser(user: User): Single<User> {
        return Single.create<User> { emitter ->
            log.debug("Update a user $user")
            dslContext
                .update(tableUser)
                .set(emailField, user.email)
                .set(passwordField, user.password.joinToString(separator = ""))
                .set(lastNameField, user.lastName)
                .set(firstNameField, user.firstName)
                .set(allowedField, user.allowed)
                .where(idField.eq(user.id))
                .execute()
            emitter.onSuccess(user)
        }
    }

    override fun ping(): Boolean {
        val record = dslContext.resultQuery("SELECT 1").fetch()
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
