package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository

class FindTransaction(private val transactionRepository: TransactionRepository) {

	fun find(id: String, userId: String): Transaction? {
		val transaction = transactionRepository.get(id = id) ?: return null
		return if (transaction.userId == userId)
			transaction
		else
			throw TransactionUserDoNotMatch(userId, id, transaction.userId)
	}

	fun findAllForUser(userId: String): List<Transaction> {
		return transactionRepository.getAllForUser(userId)
	}
}
