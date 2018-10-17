package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.report.internal.Breakdown
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

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
            .map { transactions ->
                transactions.map { transaction ->
                    Line(
                        transactionId = transaction.id,
                        date = transaction.date,
                        currency1 = transaction.currency1,
                        currency2 = transaction.currency2,
                        type = transaction.type,
                        price = transaction.price,
                        quantity = transaction.quantity)
                }
            }
            .map { lines -> Breakdown(lines) }
            .map { breakdown ->
                val observables: Observable<Line> = Observable.fromIterable(breakdown.linesToCompute)
                    .flatMap { line ->
                        val obs1 = usdPrice(line.currency1, line.date).toObservable()
                        val obs2 = usdPrice(line.currency2, line.date).toObservable()
                        val zipped = Observable.zip(obs1, obs2, BiFunction<Double, Double, Line> { price1: Double, price2: Double ->
                            line.metadata.ignored = false
                            line.metadata.currency1UsdValue = price1
                            line.metadata.currency2UsdValue = price2
                            line.metadata.quantityCurrency2 = line.quantity * line.price
                            line
                        })
                        zipped
                    }
                observables.blockingSubscribe()
                breakdown.compute()
                Report(breakdown.totalCapitalGainShort, breakdown.totalCapitalGainLong, breakdown)
            }
            .onErrorResumeNext { throwable -> Single.error(throwable) }
    }

    private fun usdPrice(currency: Currency, date: ZonedDateTime): Single<Double> {
        return if (currency.type == Currency.Type.CRYPTO)
            priceService.currencyUsdValueAt(currency, date).map { it.second }
        else
            Single.just(1.0)
    }

    companion object {
        private val log = LoggerFactory.getLogger(GenerateReport::class.java)
    }
}
