package com.cryptax.domain.port

import com.cryptax.domain.entity.Transaction

interface TransactionRepository {
	fun add(transaction: Transaction): Transaction

	fun add(transactions: List<Transaction>): List<Transaction>

	fun get(id: String): Transaction?

	fun update(transaction: Transaction): Transaction
}
