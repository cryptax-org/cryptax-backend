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
                val currenciesUsdValue = usdValues(transaction)
                Line(currenciesUsdValue.first, currenciesUsdValue.second, 0.0, transaction)
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
                            (map as MutableMap)[line.transaction.currency1.code] = Result(line.transaction.currency1).add(line)
                        }
                    }

                    if (line.transaction.currency2.type == Currency.Type.CRYPTO) {
                        if (map.containsKey(line.transaction.currency2.code)) {
                            map[line.transaction.currency2.code]!!.add(line)
                        } else {
                            (map as MutableMap)[line.transaction.currency2.code] = Result(line.transaction.currency2).add(line)
                        }
                    }
                    // FIXME duplicated code
                    report
                }
            }
            .map { report ->
                report.pairs.entries.forEach { entry: Map.Entry<String, Result> ->
                    val result: Result = report.pairs[entry.key]!!
                    result.computeGain()
                }
                report
            }
    }

    private fun usdValues(transaction: Transaction): Pair<Double, Double> {
        var c1 = 1.0
        var c2 = 1.0
        if (transaction.currency1.type == Currency.Type.CRYPTO) {
            c1 = priceService.currencyUsdValueAt(transaction.currency1, transaction.date)
        }
        if (transaction.currency2.type == Currency.Type.CRYPTO) {
            c2 = priceService.currencyUsdValueAt(transaction.currency2, transaction.date)
        }
        return Pair(c1, c2)
    }
}
