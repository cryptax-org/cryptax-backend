package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.validation.Validation
import com.cryptax.domain.entity.Source
import com.cryptax.parser.BinanceParser
import com.cryptax.parser.CoinbaseParser
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.DeleteTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.InputStream

class TransactionController(
    private val addTransaction: AddTransaction,
    private val updateTransaction: UpdateTransaction,
    private val findTransaction: FindTransaction,
    private val deleteTransaction: DeleteTransaction) {

    fun addTransaction(userId: String, transactionWeb: TransactionWeb): Single<TransactionWeb> {
        return Validation.validateTransaction(transactionWeb)
            .flatMap { addTransaction.add(transactionWeb.toTransaction(userId)) }
            .map { transaction -> TransactionWeb.toTransactionWeb(transaction) }
    }

    fun addMultipleTransactions(userId: String, transactionsWeb: List<TransactionWeb>): Single<List<TransactionWeb>> {
        return Validation.validateTransactions(transactionsWeb)
            .flatMap { addTransaction.addMultiple(transactionsWeb.map { transactionWeb -> transactionWeb.toTransaction(userId) }) }
            .map { transactions -> transactions.map { transaction -> TransactionWeb.toTransactionWeb(transaction) } }
    }

    fun updateTransaction(id: String, userId: String, transactionWeb: TransactionWeb): Single<TransactionWeb> {
        return updateTransaction
            .update(transactionWeb.toTransaction(transactionId = id, userId = userId))
            .map { transaction -> TransactionWeb.toTransactionWeb(transaction) }
    }

    fun getTransaction(id: String, userId: String): Maybe<TransactionWeb> {
        return findTransaction
            .find(id = id, userId = userId)
            .map { transaction -> TransactionWeb.toTransactionWeb(transaction) }
    }

    fun getAllTransactions(userId: String): Single<List<TransactionWeb>> {
        return findTransaction.findAllForUser(userId).map { tx ->
            tx.map { TransactionWeb.toTransactionWeb(it) }
        }
    }

    fun deleteTransaction(id: String, userId: String): Single<Unit> {
        return deleteTransaction.delete(id, userId)
    }

    fun uploadCSVTransactions(inputStream: InputStream, userId: String, source: Source, delimiter: Char = ','): Single<List<TransactionWeb>> {
        val transactions = when (source) {
            Source.BINANCE -> BinanceParser(delimiter = delimiter).parse(inputStream, userId)
            Source.COINBASE -> CoinbaseParser(delimiter = delimiter).parse(inputStream, userId)
            else -> throw RuntimeException("Source [$source] not handled")
        }
        return addTransaction
            .addMultiple(transactions)
            .map { tx -> tx.map { TransactionWeb.toTransactionWeb(it) } }
    }
}
