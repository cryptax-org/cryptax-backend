package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.usecase.transaction.AddTransaction

class TransactionController(private val addTransaction: AddTransaction) {

	fun addTransaction(userId: String, transactionWeb: TransactionWeb): TransactionWeb {
		val result = addTransaction.add(transactionWeb.toTransaction(userId))
		return TransactionWeb.toTransactionWeb(result)
	}
}
