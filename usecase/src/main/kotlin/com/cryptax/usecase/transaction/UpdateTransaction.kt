package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.validator.validateUpdateTransaction

class UpdateTransaction(private val transactionRepository: TransactionRepository) {

    fun update(transaction: Transaction): Transaction {
        validateUpdateTransaction(transaction)
        val transactionId = transaction.id!!
        val transactionDb = transactionRepository.get(transactionId) ?: throw TransactionNotFound(transactionId)
        if (transactionDb.userId != transaction.userId) {
            throw TransactionUserDoNotMatch(transaction.userId, transactionId, transactionDb.userId)
        }
        return transactionRepository.update(transaction)
    }
}
