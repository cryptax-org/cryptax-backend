package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.UserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.validator.validateUpdateTransaction

class UpdateTransaction(private val transactionRepository: TransactionRepository) {

	fun update(transaction: Transaction): Transaction {
		validateUpdateTransaction(transaction)
		val transactionDb = transactionRepository.get(transaction.id!!) ?: throw TransactionNotFound(transaction.id!!)
		if (transactionDb.userId != transaction.userId) {
			throw UserDoNotMatch("User [${transaction.userId}] tried to update [${transaction.id}], but that transaction is owned by [${transactionDb.userId}]")
		}
		return transactionRepository.update(transaction)
	}
}
