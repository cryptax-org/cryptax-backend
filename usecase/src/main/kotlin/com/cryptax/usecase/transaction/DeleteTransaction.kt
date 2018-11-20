package com.cryptax.usecase.transaction

import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class DeleteTransaction(private val transactionRepository: TransactionRepository) {

    fun delete(id: String, userId: String): Single<Unit> {
        log.info("Usecase, delete a transaction $id")
        return transactionRepository
            .get(id)
            .observeOn(Schedulers.computation())
            .map { transaction ->
                if (transaction.userId != userId) {
                    throw TransactionUserDoNotMatch(userId, id, transaction.userId)
                }
                transaction
            }
            .flatMapSingle { transactionRepository.delete(id) }
            .observeOn(Schedulers.computation())
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is NoSuchElementException -> Single.error(TransactionNotFound(id))
                    else -> Single.error(throwable)
                }
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DeleteTransaction::class.java)
    }
}
