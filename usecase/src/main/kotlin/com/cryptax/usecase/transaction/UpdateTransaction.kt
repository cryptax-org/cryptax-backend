package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.validator.validateUpdateTransaction
import io.reactivex.Single

class UpdateTransaction(private val transactionRepository: TransactionRepository) {

    fun update(transaction: Transaction): Single<Transaction> {
        validateUpdateTransaction(transaction)
        return transactionRepository
            .get(transaction.id!!)
            .map { transactionDb ->
                if (transactionDb.userId != transaction.userId) {
                    throw TransactionUserDoNotMatch(transaction.userId, transaction.id!!, transactionDb.userId)
                }
                transactionDb
            }
            .flatMapSingle { transactionRepository.update(transaction) }
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(TransactionNotFound(transaction.id!!))
                    else -> Single.error(throwable)
                }
            }
    }
}
