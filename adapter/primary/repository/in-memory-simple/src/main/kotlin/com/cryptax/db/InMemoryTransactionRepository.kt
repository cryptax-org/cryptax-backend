package com.cryptax.db

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InMemoryTransactionRepository : TransactionRepository {

    private val inMemoryDb = HashMap<String, Transaction>()

    override fun add(transaction: Transaction): Single<Transaction> {
        return Single.fromCallable {
            log.debug("Create a transaction $transaction")
            inMemoryDb[transaction.id] = transaction
            transaction
        }
    }

    override fun add(transactions: List<Transaction>): Single<List<Transaction>> {
        return Single.fromCallable {
            log.debug("Add transactions")
            transactions.forEach {
                inMemoryDb[it.id] = it
            }
            transactions
        }
    }

    override fun get(id: String): Maybe<Transaction> {
        return Maybe.defer {
            log.debug("Get a transaction by id [$id]")
            val transaction = inMemoryDb[id]
            when (transaction) {
                null -> Maybe.empty<Transaction>()
                else -> Maybe.just(transaction)
            }
        }
    }

    override fun getAllForUser(userId: String): Single<List<Transaction>> {
        return Single.fromCallable { inMemoryDb.values.filter { value -> value.userId == userId } }
    }

    override fun update(transaction: Transaction): Single<Transaction> {
        return Single.fromCallable {
            inMemoryDb[transaction.id] = transaction
            transaction
        }
    }

    override fun delete(id: String): Single<Unit> {
        return Single.fromCallable {
            inMemoryDb.remove(id)
            Unit
        }
    }

    fun deleteAll() {
        inMemoryDb.clear()
    }

    override fun ping(): Boolean {
        return true
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(InMemoryTransactionRepository::class.java)
    }
}
