package com.cryptax.db.google

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class GoogleTransactionRepository(private val dslContext: DSLContext) : TransactionRepository {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(GoogleTransactionRepository::class.java)
        private val zoneId = ZoneId.of("UTC")
        private val tableTransaction = table(name("transaction"))
        private val tableUser = table(name("user"))
        private val idField = field(name("id"), SQLDataType.VARCHAR)
        private val userIdField = field(name("userId"), SQLDataType.VARCHAR.nullable(false))
        private val sourceField = field(name("source"), SQLDataType.VARCHAR.nullable(false))
        private val dateField = field(name("date"), SQLDataType.OFFSETDATETIME.nullable(false))
        private val typeField = field(name("type"), SQLDataType.VARCHAR.nullable(false))
        private val priceField = field(name("price"), SQLDataType.DOUBLE.nullable(false))
        private val quantityField = field(name("quantity"), SQLDataType.DOUBLE.nullable(false))
        private val currency1Field = field(name("currency1"), SQLDataType.VARCHAR.nullable(false))
        private val currency2Field = field(name("currency2"), SQLDataType.VARCHAR.nullable(false))
    }

    init {
        dslContext
            .createTableIfNotExists(tableTransaction)
            .columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)
            .constraints(
                constraint("PK_TRANSACTION").primaryKey(idField),
                constraint("FK_USER_ID_TRANSACTION").foreignKey(userIdField).references(tableUser, field(name("id"), SQLDataType.VARCHAR))
            )
            .execute()
    }

    override fun add(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Create a transaction $transaction")
            dslContext
                .insertInto(tableTransaction)
                .columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)
                .values(
                    transaction.id,
                    transaction.userId,
                    transaction.source.name,
                    OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId),
                    transaction.type.name,
                    transaction.price,
                    transaction.quantity,
                    transaction.currency1.name,
                    transaction.currency2.name)
                .execute()
            emitter.onSuccess(transaction)
        }
    }

    override fun add(transactions: List<Transaction>): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Add transactions")
            transactions.forEach { transaction ->
                // TODO this looks a bit ugly
                add(transaction).blockingGet()
            }
            emitter.onSuccess(transactions)
        }
    }

    override fun get(id: String): Maybe<Transaction> {
        return Maybe.create<Transaction> { emitter ->
            log.debug("Get a transaction by id [$id]")
            val record = dslContext
                .selectFrom(tableTransaction)
                .where(idField.eq(id))
                .fetchOne()
            when (record) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toTransaction(record))
            }
        }
    }

    override fun getAllForUser(userId: String): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Get all transactions for [$userId]")
            val results: Result<Record> = dslContext
                .selectFrom(tableTransaction)
                .where(userIdField.eq(userId))
                .fetch()
            when {
                results.isEmpty() -> emitter.onSuccess(listOf())
                else -> emitter.onSuccess(results.map { r -> toTransaction(r) })
            }
        }
    }

    override fun update(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Update one transaction [${transaction.id}]")
            dslContext
                .update(tableTransaction)
                .set(userIdField, transaction.userId)
                .set(sourceField, transaction.source.name)
                .set(dateField, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId))
                .set(typeField, transaction.type.name)
                .set(priceField, transaction.price)
                .set(quantityField, transaction.quantity)
                .set(currency1Field, transaction.currency1.name)
                .set(currency2Field, transaction.currency2.name)
                .where(idField.eq(transaction.id))
                .execute()
            emitter.onSuccess(transaction)
        }
    }

    override fun ping(): Boolean {
        val record = dslContext.resultQuery("SELECT 1").fetch()
        return record.isNotEmpty
    }

    private fun toTransaction(record: Record): Transaction {
        val offsetDateTime = record.get(dateField, OffsetDateTime::class.java)
        return Transaction(
            record.get(idField, String::class.java),
            record.get(userIdField, String::class.java),
            Source.valueOf(record.get(sourceField, String::class.java)),
            ZonedDateTime.ofInstant(offsetDateTime.toInstant(), zoneId),
            Transaction.Type.valueOf(record.get(typeField, String::class.java)),
            record.get(priceField, Double::class.java),
            record.get(quantityField, Double::class.java),
            Currency.findCurrency(record.get(currency1Field, String::class.java)),
            Currency.findCurrency(record.get(currency2Field, String::class.java))
        )
    }
}
