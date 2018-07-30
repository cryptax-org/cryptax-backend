package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Details
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger("LinesToReport")

fun linesToReport(lines: List<Line>): Single<Report> {
    return Observable
        .fromIterable(lines)
        .collectInto(HashMap()) { currencyMap: Map<Currency, Details>, line: Line ->
            val map = currencyMap as HashMap
            for (currency in line.currencies()) {
                val lineCopy = line.copy()
                if (map.containsKey(currency)) {
                    map[currency]!!.add(lineCopy)
                } else {
                    map[currency] = Details().add(lineCopy)
                }
            }
            map.values.forEach { list -> list.sortByDate() }
        }
        .map { map: Map<Currency, Details> ->
            val totalGainsLosses = map.keys
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .map { currency -> computeGainsLosses(currency, map[currency]!!) }
                .sum()
            Report(totalGainsLosses, map)
        }
}

internal fun computeGainsLosses(currency: Currency, details: Details): Double {
    val coinsOwned: List<CoinsOwned> = extractCoinsOwned(currency, details.lines)
    val gainsLosses = details.lines
        .filter { line -> line.currency2 == currency && line.type == Transaction.Type.BUY }
        .filter { currency.type == Currency.Type.CRYPTO }
        .map { line ->
            val currentPrice = line.metadata.currency2UsdValue * line.quantity * line.price
            val originalPrice = getOriginalPrice(coinsOwned, line)
            line.metadata.ignored = false
            line.metadata.currentPrice = currentPrice
            line.metadata.originalPrice = originalPrice
            line.metadata.gainsLosses = currentPrice - originalPrice
            line.metadata.gainsLosses!!
        }
        .sum()
    details.gainsLosses = gainsLosses
    return details.gainsLosses
}

internal fun extractCoinsOwned(currency: Currency, lines: List<Line>): List<CoinsOwned> {
    return lines
        .filter { line -> line.currency1 == currency && line.type == Transaction.Type.BUY }
        .map { line -> CoinsOwned(line.date, line.price, line.quantity) }
}

internal fun getOriginalPrice(coinsOwned: List<CoinsOwned>, line: Line): Double {
    return getOriginalPrice(coinsOwned, 0, line.metadata.quantityCurrency2)
}

private fun getOriginalPrice(coinsOwned: List<CoinsOwned>, index: Int, value: Double): Double {
    val coin = coinsOwned[index]
    return if (coin.quantity >= value) {
        coin.quantity = coin.quantity - value
        coin.price * value
    } else {
        if (index < coinsOwned.size) {
            val currentValue = coin.price * coin.quantity
            val rest = value - coin.quantity
            coin.quantity = 0.0
            currentValue + getOriginalPrice(coinsOwned, index + 1, rest)
        } else {
            throw RuntimeException("Not enough coin")
        }
    }
}

class CoinsOwned(val date: ZonedDateTime, val price: Double, var quantity: Double)
