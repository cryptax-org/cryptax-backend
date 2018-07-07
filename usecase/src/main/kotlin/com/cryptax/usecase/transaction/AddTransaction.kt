package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateAddTransaction

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
}
