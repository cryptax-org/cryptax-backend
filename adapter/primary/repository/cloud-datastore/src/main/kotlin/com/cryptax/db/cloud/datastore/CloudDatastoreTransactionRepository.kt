package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository
import com.google.cloud.Timestamp
import com.google.cloud.datastore.Datastore
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

class CloudDatastoreTransactionRepository(private val datastore: Datastore) : TransactionRepository {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudDatastoreTransactionRepository::class.java)
        private val zoneId = ZoneId.of("UTC")
        private val kind = Transaction::class.java.simpleName
    }

    override fun add(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Create a transaction $transaction")
            val entity = toEntity(
                key(transaction.id),
                transaction)
            datastore.put(entity)
            emitter.onSuccess(transaction)
        }
    }

    override fun add(transactions: List<Transaction>): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Add transactions")
            val entities = transactions.map { transaction ->
                toEntity(
                    key(transaction.id),
                    transaction)
            }
                .toTypedArray()
            datastore.put(*entities)
            emitter.onSuccess(transactions)
        }
    }

    override fun get(id: String): Maybe<Transaction> {
        return Maybe.create<Transaction> { emitter ->
            log.debug("Get a transaction by id [$id]")
            val entity = datastore.get(key(id))
            when (entity) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(toTransaction(entity))
            }
        }
    }

    override fun getAllForUser(userId: String): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Get all transactions for [$userId]")
            val query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
                .build()
            val queryResults: QueryResults<Entity> = datastore.run(query)
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
            log.debug("Update one transaction [${transaction.id}]")
            datastore.update(toEntity(key(transaction.id), transaction))
            emitter.onSuccess(transaction)
        }
    }

    override fun ping(): Boolean {
        // FIXME to implement
        return false
    }

    private fun key(id: String): Key {
        return datastore.newKeyFactory().setKind(kind).newKey(id)
    }

    private fun toEntity(key: Key, transaction: Transaction): Entity {
        return Entity.newBuilder(key)
            .set("userId", transaction.userId)
            .set("source", transaction.source.name)
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
            source = Source.valueOf(entity.getString("source")),
            date = date.toInstant().atZone(zoneId),
            type = Transaction.Type.valueOf(entity.getString("type")),
            price = entity.getDouble("price"),
            quantity = entity.getDouble("quantity"),
            currency1 = Currency.findCurrency(entity.getString("currency1")),
            currency2 = Currency.findCurrency(entity.getString("currency2"))
        )
    }
}
