package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction

class TransactionController(
	private val addTransaction: AddTransaction,
	private val updateTransaction: UpdateTransaction,
	private val findTransaction: FindTransaction) {

	fun addTransaction(userId: String, transactionWeb: TransactionWeb): TransactionWeb {
		val result = addTransaction.add(transactionWeb.toTransaction(userId))
		return TransactionWeb.toTransactionWeb(result)
	}

	fun updateTransaction(id: String, userId: String, transactionWeb: TransactionWeb): TransactionWeb {
		val result = updateTransaction.update(transactionWeb.toTransaction(transactionId = id, userId = userId))
		return TransactionWeb.toTransactionWeb(result)
	}

	fun getTransaction(id: String, userId: String): TransactionWeb? {
		val result = findTransaction.find(id = id, userId = userId)
		return if (result != null) {
			TransactionWeb.toTransactionWeb(result)
		} else {
			null
		}
	}

	fun getAllTransactions(userId: String): List<TransactionWeb> {
		return findTransaction.findAllForUser(userId).map { TransactionWeb.toTransactionWeb(it) }
	}
}
