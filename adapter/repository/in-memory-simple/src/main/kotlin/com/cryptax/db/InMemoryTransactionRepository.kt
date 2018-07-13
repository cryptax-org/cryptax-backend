package com.cryptax.db

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository

class InMemoryTransactionRepository : TransactionRepository {

    private val inMemoryDb = HashMap<String, Transaction>()

    override fun add(transaction: Transaction): Transaction {
        inMemoryDb[transaction.id!!] = transaction
        return transaction
    }

    override fun add(transactions: List<Transaction>): List<Transaction> {
        transactions.forEach {
            inMemoryDb[it.id!!] = it
        }
        return transactions
    }

    override fun get(id: String): Transaction? {
        return inMemoryDb[id]
    }

    override fun getAllForUser(userId: String): List<Transaction> {
        return inMemoryDb.values.filter { value -> value.userId == userId }
    }

    override fun update(transaction: Transaction): Transaction {
        inMemoryDb[transaction.id!!] = transaction
        return transaction
    }
}
