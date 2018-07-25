package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Function

class FindTransaction(private val transactionRepository: TransactionRepository) {

    fun find(id: String, userId: String): Maybe<Transaction> {
        return transactionRepository
            .get(id)
            .map { transaction ->
                if (transaction.userId != userId) {
                    throw TransactionUserDoNotMatch(userId, id, transaction.userId)
                }
                transaction
            }
            .onErrorResumeNext(Function { throwable -> Maybe.error(throwable) })
    }

    fun findAllForUser(userId: String): Single<List<Transaction>> {
        return transactionRepository.getAllForUser(userId)
    }
}
