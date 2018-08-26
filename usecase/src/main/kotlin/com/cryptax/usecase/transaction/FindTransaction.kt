package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Function
import org.slf4j.LoggerFactory

class FindTransaction(private val transactionRepository: TransactionRepository) {

    companion object {
        private val log = LoggerFactory.getLogger(FindTransaction::class.java)
    }

    fun find(id: String, userId: String): Maybe<Transaction> {
        log.info("Usecase, find a transaction $id, for user $userId")
        return transactionRepository
            .get(id)
            .toSingle()
            .map { transaction ->
                if (transaction.userId != userId) {
                    throw TransactionUserDoNotMatch(userId, id, transaction.userId)
                }
                transaction
            }
            .toMaybe()
            .onErrorResumeNext(Function { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Maybe.error(TransactionNotFound(id))
                    else -> Maybe.error(throwable)
                }
            })
    }

    fun findAllForUser(userId: String): Single<List<Transaction>> {
        return transactionRepository.getAllForUser(userId)
    }
}
