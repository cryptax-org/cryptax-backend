package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateAddTransaction
import com.cryptax.usecase.validator.validateAddTransactions

class AddTransaction(
	private val transactionRepository: TransactionRepository,
	private val userRepository: UserRepository,
	private val idGenerator: IdGenerator) {

	fun add(transaction: Transaction): Transaction {
		validateAddTransaction(transaction)
		userRepository.findById(transaction.userId) ?: throw UserNotFoundException(transaction.userId)

		val transactionToSave = Transaction(
			id = idGenerator.generate(),
			userId = transaction.userId,
			source = transaction.source,
			date = transaction.date,
			type = transaction.type,
			price = transaction.price,
			amount = transaction.amount,
			currency1 = transaction.currency1,
			currency2 = transaction.currency2)

		return transactionRepository.add(transactionToSave)
	}

	fun addMultiple(transactions: List<Transaction>): List<Transaction> {
		validateAddTransactions(transactions)
		userRepository.findById(transactions[0].userId) ?: throw UserNotFoundException(transactions[0].userId)

		val transactionsToSave = transactions
			.map {
				Transaction(
					id = idGenerator.generate(),
					userId = it.userId,
					source = it.source,
					date = it.date,
					type = it.type,
					price = it.price,
					amount = it.amount,
					currency1 = it.currency1,
					currency2 = it.currency2)
			}
		return transactionRepository.add(transactionsToSave)
	}
}
