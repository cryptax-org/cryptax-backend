package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(GenerateReport::class.java)

class GenerateReport(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val priceService: PriceService
) {

    fun generate(userId: String): Single<Report> {
        log.info("Usecase, generate a report for $userId")
        return userRepository.findById(userId)
            .subscribeOn(Schedulers.io())
            .isEmpty
            .flatMap { isEmpty ->
                when (isEmpty) {
                    true -> throw UserNotFoundException(userId)
                    else -> transactionRepository.getAllForUser(userId)
                }
            }
            .toFlowable()
            .flatMap { transactions -> Flowable.fromIterable(transactions) }
            .parallel(10)
            .runOn(Schedulers.io())
            .map { transaction -> Line(transaction, getUsdAmount(transaction)) }
            .sequential()
            .collectInto(Report()) { report: Report, line: Line -> report.lines.add(line) }
    }

    private fun getUsdAmount(transaction: Transaction): Double {
        return if (transaction.currency1 == Currency.USD || transaction.currency2 == Currency.USD) {
            val amountDollars = transaction.quantity * transaction.price
            if (transaction.type == Transaction.Type.BUY) amountDollars else -amountDollars
        } else {
            priceService.getUsdAmount(transaction)
        }
    }
}
