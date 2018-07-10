package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.UpdateTransaction

class TransactionController(private val addTransaction: AddTransaction, private val updateTransaction: UpdateTransaction) {

	fun addTransaction(userId: String, transactionWeb: TransactionWeb): TransactionWeb {
		val result = addTransaction.add(transactionWeb.toTransaction(userId))
		return TransactionWeb.toTransactionWeb(result)
	}

	fun updateTransaction(transactionId: String, userId: String, transactionWeb: TransactionWeb): TransactionWeb {
		val result = updateTransaction.update(transactionWeb.toTransaction(transactionId = transactionId, userId = userId))
		return TransactionWeb.toTransactionWeb(result)
	}
}
