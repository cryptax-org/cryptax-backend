package com.cryptax.db

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(InMemoryTransactionRepository::class.java)

class InMemoryTransactionRepository : TransactionRepository {

    private val inMemoryDb = HashMap<String, Transaction>()

    override fun add(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            log.debug("Create a transaction $transaction")
            inMemoryDb[transaction.id!!] = transaction
            emitter.onSuccess(transaction)
        }
    }

    override fun add(transactions: List<Transaction>): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            log.debug("Add transactions")
            transactions.forEach {
                inMemoryDb[it.id!!] = it
            }
            emitter.onSuccess(transactions)
        }
    }

    override fun get(id: String): Maybe<Transaction> {
        return Maybe.create<Transaction> { emitter ->
            log.debug("Get a transaction by id [$id]")
            val transaction = inMemoryDb[id]
            when (transaction) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(transaction)
            }
        }
    }

    override fun getAllForUser(userId: String): Single<List<Transaction>> {
        return Single.create<List<Transaction>> { emitter ->
            val transactions = inMemoryDb.values.filter { value -> value.userId == userId }
            emitter.onSuccess(transactions)
        }
    }

    override fun update(transaction: Transaction): Single<Transaction> {
        return Single.create<Transaction> { emitter ->
            inMemoryDb[transaction.id!!] = transaction
            emitter.onSuccess(transaction)
        }
    }

    override fun ping(): Boolean {
        return true
    }
}
