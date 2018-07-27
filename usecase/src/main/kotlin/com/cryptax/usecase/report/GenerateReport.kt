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
import java.util.TreeSet

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
            .map { transaction ->
                val amountResult = getUsdAmount(transaction)
                Line(amountResult.second, amountResult.first, transaction)
            }
            .sequential()
            .collectInto(Report()) { report: Report, line: Line ->
                run {
                    val map = report.pairs
                    val key = line.transaction.currency1.code + "/" + line.transaction.currency2.code
                    val key2 = line.transaction.currency2.code + "/" + line.transaction.currency1.code
                    when {
                        map.containsKey(key) -> map[key]!!.add(line)
                        map.containsKey(key2) -> map[key2]!!.add(line)
                        else -> {
                            val set = TreeSet<Line>(Comparator { o1, o2 -> o1.transaction.date.compareTo(o2.transaction.date) })
                            set.add(line)
                            (map as HashMap)[key] = set
                        }
                    }
                    report
                }
            }
    }

    private fun getUsdAmount(transaction: Transaction): Pair<String?, Double> {
        return if (transaction.currency1 == Currency.USD || transaction.currency2 == Currency.USD) {
            val amountDollars = transaction.quantity * transaction.price
            if (transaction.type == Transaction.Type.BUY) Pair(null, amountDollars) else Pair(null, -amountDollars)
        } else {
            priceService.getUsdAmount(transaction)
        }
    }
}
