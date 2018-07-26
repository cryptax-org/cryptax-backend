package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateAddTransaction
import com.cryptax.usecase.validator.validateAddTransactions
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(AddTransaction::class.java)

class AddTransaction(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val idGenerator: IdGenerator) {

    fun add(transaction: Transaction): Single<Transaction> {
        log.info("Usecase, add a transaction $transaction")
        return validateAddTransaction(transaction)
            .flatMap { userRepository.findById(transaction.userId).isEmpty }
            .map { isEmpty ->
                when (isEmpty) {
                    true -> throw UserNotFoundException(transaction.userId)
                    else -> Transaction(
                        id = idGenerator.generate(),
                        userId = transaction.userId,
                        source = transaction.source,
                        date = transaction.date,
                        type = transaction.type,
                        price = transaction.price,
                        quantity = transaction.quantity,
                        currency1 = transaction.currency1,
                        currency2 = transaction.currency2)
                }
            }
            .flatMap { t -> transactionRepository.add(t) }
    }

    fun addMultiple(transactions: List<Transaction>): Single<List<Transaction>> {
        log.info("Usecase, add a list of transactions $transactions")
        return validateAddTransactions(transactions)
            .flatMap {
                userRepository.findById(transactions[0].userId)
                    .subscribeOn(Schedulers.io())
                    .isEmpty
                    .map { isEmpty ->
                        when (isEmpty) {
                            true -> throw UserNotFoundException(transactions[0].userId)
                            else -> transactions.map {
                                Transaction(
                                    id = idGenerator.generate(),
                                    userId = it.userId,
                                    source = it.source,
                                    date = it.date,
                                    type = it.type,
                                    price = it.price,
                                    quantity = it.quantity,
                                    currency1 = it.currency1,
                                    currency2 = it.currency2)
                            }
                        }
                    }
                    .flatMap { transactionToSave: List<Transaction> -> transactionRepository.add(transactionToSave) }
            }
    }
}
