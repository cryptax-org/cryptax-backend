package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import io.reactivex.Observable
import io.reactivex.Single

class GenerateReport(private val transactionRepository: TransactionRepository, private val priceService: PriceService) {

    fun generate(userId: String): Single<Report> {
        return transactionRepository
            .getAllForUser(userId)
            .flatMapObservable { transactions -> Observable.fromIterable(transactions) }
            .map { transaction -> Line(transaction, getDollarsAmount(transaction)) }
            .collectInto(Report()) { report: Report, line: Line -> report.lines.add(line) }
    }

    private fun getDollarsAmount(transaction: Transaction): Double {
        return if (transaction.currency1 == Currency.USD || transaction.currency2 == Currency.USD) {
            val amountDollars = transaction.amount * transaction.price
            if (transaction.type == Transaction.Type.BUY) amountDollars else -amountDollars
        } else {
            priceService.getPriceInDollars(transaction)
        }
    }
}
