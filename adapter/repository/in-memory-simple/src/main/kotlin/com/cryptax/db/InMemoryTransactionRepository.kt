package com.cryptax.db

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.TransactionRepository

class InMemoryTransactionRepository : TransactionRepository {

	private val inMemoryDb = HashMap<String, Transaction>()

	override fun add(transaction: Transaction): Transaction {
		inMemoryDb[transaction.id!!] = transaction
		return transaction
	}
}
