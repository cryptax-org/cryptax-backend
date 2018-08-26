package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository
import com.google.cloud.Timestamp
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StructuredQuery
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneId

class CloudDatastoreTransactionRepository(datastore: Datastore) : TransactionRepository, CloudDatastore(datastore) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudDatastoreTransactionRepository::class.java)
        private val zoneId = ZoneId.of("UTC")
        private val kind = Transaction::class.java.simpleName
    }

    override fun add(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Create a transaction $transaction")
            datastore.put(toEntity(transaction))
            emitter.onSuccess(transaction)
        }
    }

    override fun add(transactions: List<Transaction>): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Add transactions")
            val entities = transactions.map { transaction -> toEntity(transaction) }.toTypedArray()
            datastore.put(*entities)
            emitter.onSuccess(transactions)
        }
    }

    override fun get(id: String): Maybe<Transaction> {
        return Maybe.create<Transaction> { emitter ->
            log.debug("Get a transaction by id $id")
            val entity = datastore.get(key(id))
            when (entity) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toTransaction(entity))
            }
        }
    }

    override fun getAllForUser(userId: String): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Get all transactions for $userId")
            val query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
                .build()
            val queryResults = datastore.run(query)
            val result = mutableListOf<Transaction>()
            while (queryResults.hasNext()) {
                result.add(toTransaction(queryResults.next()))
            }
            when {
                result.isEmpty() -> emitter.onSuccess(listOf())
                else -> emitter.onSuccess(result)
            }
        }
    }

    override fun update(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Update one transaction ${transaction.id}")
            datastore.update(toEntity(transaction))
            emitter.onSuccess(transaction)
        }
    }

    override fun delete(id: String): Single<Unit> {
        return Single.create<Unit> { emitter ->
            log.debug("Delete one transaction $id")
            datastore.delete(key(id))
            emitter.onSuccess(Unit)
        }
    }

    override fun ping(): Boolean {
        log.debug("Ping transaction repository")
        return try {
            datastore.run(Query.newGqlQueryBuilder("SELECT userId FROM $kind LIMIT 1").setAllowLiteral(true).build())
            true
        } catch (e: DatastoreException) {
            log.error("Could not ping Google Cloud", e)
            false
        }
    }

    private fun key(id: String): Key {
        return datastore.newKeyFactory().setKind(kind).newKey(id)
    }

    private fun toEntity(transaction: Transaction): Entity {
        return Entity.newBuilder(key(transaction.id))
            .set("userId", transaction.userId)
            .set("source", transaction.source)
            .set("date", Timestamp.ofTimeSecondsAndNanos(transaction.date.toInstant().epochSecond, transaction.date.toInstant().nano))
            .set("type", transaction.type.name)
            .set("price", transaction.price)
            .set("quantity", transaction.quantity)
            .set("currency1", transaction.currency1.code)
            .set("currency2", transaction.currency2.code)
            .build()
    }

    private fun toTransaction(entity: Entity): Transaction {
        val date = entity.getTimestamp("date").toDate()
        return Transaction(
            id = entity.key.name,
            userId = entity.getString("userId"),
            source = entity.getString("source"),
            date = date.toInstant().atZone(zoneId),
            type = Transaction.Type.valueOf(entity.getString("type")),
            price = entity.getDouble("price"),
            quantity = entity.getDouble("quantity"),
            currency1 = Currency.findCurrency(entity.getString("currency1")),
            currency2 = Currency.findCurrency(entity.getString("currency2"))
        )
    }
}
