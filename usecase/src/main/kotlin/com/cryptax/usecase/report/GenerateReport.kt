package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.FinalReport
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.report.internal.linesToReport
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(GenerateReport::class.java)

class GenerateReport(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val priceService: PriceService) {

    fun generate(userId: String): Single<FinalReport> {
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
                val currenciesUsdValue: Pair<Double, Double> = usdValues(transaction)
                Line(
                    currency1UsdValue = currenciesUsdValue.first,
                    currency2UsdValue = currenciesUsdValue.second,
                    transaction = transaction
                )
            }
            .toList()
            .observeOn(Schedulers.computation())
            .flatMap { linesToReport(it) }
        //.sequential()
    }

    private fun usdValues(transaction: Transaction): Pair<Double, Double> {
        val c1 = if (transaction.currency1.type == Currency.Type.CRYPTO)
            priceService.currencyUsdValueAt(transaction.currency1, transaction.date).second
        else
            1.0
        val c2 = if (transaction.currency2.type == Currency.Type.CRYPTO)
            priceService.currencyUsdValueAt(transaction.currency2, transaction.date).second
        else
            1.0
        return Pair(c1, c2)
    }
}
