package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.report.internal.Breakdown
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

private val log: Logger = LoggerFactory.getLogger(GenerateReport::class.java)

class GenerateReport(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val priceService: PriceService) {

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
                val currenciesUsd = usdValuesAt(transaction.date, transaction.currency1, transaction.currency2)
                Line(
                    transactionId = transaction.id!!,
                    date = transaction.date,
                    currency1 = transaction.currency1,
                    currency2 = transaction.currency2,
                    type = transaction.type,
                    price = transaction.price,
                    quantity = transaction.quantity,
                    currency1UsdValue = currenciesUsd.first,
                    currency2UsdValue = currenciesUsd.second)
            }
            .toList()
            .observeOn(Schedulers.computation())
            .flatMap { lines ->
                Single.just(lines)
                    .map { Breakdown(it) }
                    .map { breakdown ->
                        breakdown.compute()
                        val totalCapitalGainShort = breakdown.keys
                            .filter { currency -> currency.type == Currency.Type.CRYPTO }
                            .map { currency -> breakdown[currency]!! }
                            .map { details -> details.capitalGainShort }
                            .sum()

                        val totalCapitalGainLong = breakdown.keys
                            .filter { currency -> currency.type == Currency.Type.CRYPTO }
                            .map { currency -> breakdown[currency]!! }
                            .map { details -> details.capitalGainLong }
                            .sum()

                        Report(totalCapitalGainShort = totalCapitalGainShort, totalCapitalGainLong = totalCapitalGainLong, breakdown = breakdown)
                    }
            }
        //.sequential()
    }

    private fun usdValuesAt(date: ZonedDateTime, currency1: Currency, currency2: Currency): Pair<Double, Double> {
        val c1 = if (currency1.type == Currency.Type.CRYPTO)
            priceService.currencyUsdValueAt(currency1, date).second
        else
            1.0
        val c2 = if (currency2.type == Currency.Type.CRYPTO)
            priceService.currencyUsdValueAt(currency2, date).second
        else
            1.0
        return Pair(c1, c2)
    }
}
