package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Result
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
            //.parallel(2)
            //.runOn(Schedulers.io())
            .map { transaction ->
                val amountResult = getUsdAmount(transaction)
                Line(amountResult.first, amountResult.second, amountResult.third, transaction)
            }
            //.sequential()
            .collectInto(Report()) { report: Report, line: Line ->
                run {
                    val map = report.pairs

                    // FIXME duplicated code
                    if (line.transaction.currency1.type == Currency.Type.CRYPTO) {
                        if (map.containsKey(line.transaction.currency1.code)) {
                            map[line.transaction.currency1.code]!!.add(line)
                        } else {
                            (map as MutableMap)[line.transaction.currency1.code] = Result().add(line)
                        }
                    }

                    if (line.transaction.currency2.type == Currency.Type.CRYPTO) {
                        if (map.containsKey(line.transaction.currency2.code)) {
                            map[line.transaction.currency2.code]!!.add(line)
                        } else {
                            (map as MutableMap)[line.transaction.currency2.code] = Result().add(line)
                        }
                    }
                    // FIXME duplicated code
                    report
                }
            }
            .map { report ->
                report.pairs.entries.forEach { entry: Map.Entry<String, Result> ->
                    val result = report.pairs[entry.key]
                    result!!.lines.forEach { line ->
                        if (line.transaction.currency2.code == entry.key && line.transaction.type == Transaction.Type.BUY) {
                            val gain = line.usdCurrency2 - getOriginalPrice()
                            log.debug("Gain found: $gain")
                            result.gain += gain
                        }
                    }
                }

                report
            }
    }

    private fun getOriginalPrice(): Double {
        return 200.0
    }

    private fun getUsdAmount(transaction: Transaction): Triple<String?, Double, Double> {
        return if (transaction.currency1 == Currency.USD || transaction.currency2 == Currency.USD) {
            val amountDollars = transaction.quantity * transaction.price
            if (transaction.type == Transaction.Type.BUY) Triple(null, amountDollars, 1.0) else Triple(null, -amountDollars, 1.0)
        } else {
            priceService.getUsdAmount(transaction)
        }
    }
}
