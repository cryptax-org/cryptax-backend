package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.FinalReport
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Lines
import com.cryptax.domain.entity.Transaction
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger("LinesToReport")

fun linesToReport(lines: List<Line>): Single<FinalReport> {
    return Observable
        .fromIterable(lines)
        .collectInto(HashMap()) { currencyMap: Map<String, Lines>, line: Line ->
            val map = currencyMap as HashMap
            for (currency in line.currencies()) {
                val lineCopy = line.copy()
                if (map.containsKey(currency.code)) {
                    map[currency.code]!!.add(lineCopy)
                } else {
                    map[currency.code] = Lines().add(lineCopy)
                }
            }
            map.values.forEach { list -> list.sortByDate() }
        }
        .map { map: Map<String, Lines> ->
            val totalGainsLosses = map.keys
                .map { str -> Currency.findCurrency(str) }
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .map { currency -> computeGainsLosses(currency, map[currency.code]!!) }
                .sum()
            FinalReport(totalGainsLosses, map)
        }
}

fun computeGainsLosses(currency: Currency, lines: Lines): Double {
    val bases: List<Base> = extractBase(currency, lines.lines)
    val gainsLosses = lines.lines
        .filter { line -> line.currency2 == currency && line.type == Transaction.Type.BUY }
        .filter { currency.type == Currency.Type.CRYPTO }
        .map { line ->
            val currentPrice = line.currency2UsdValue * line.quantity * line.price
            if (line.currency1 == Currency.CARDANO && line.currency2 == Currency.ETH) {
                log.debug("${line.currency1}/${line.currency2} Current price calculation: ${line.currency2UsdValue} * ${line.quantity} * ${line.price}")
            }
            val originalPrice = getOriginalPrice(bases, line)
            line.ignored = false
            line.currentPrice = currentPrice
            line.originalPrice = originalPrice
            line.gainsLosses = currentPrice - originalPrice
            log.debug("Gainslosses found for ${line.currency1}/${line.currency2} ${line.gainsLosses}")
            line.gainsLosses!!
        }
        .sum()
    lines.gainsLosses = gainsLosses
    return lines.gainsLosses!!
}

fun extractBase(currency: Currency, lines: List<Line>): List<Base> {
    return lines
        .filter { line -> line.currency1 == currency && line.type == Transaction.Type.BUY }
        .map { line -> Base(line.date, line.price, line.quantity) }
}

fun getOriginalPrice(bases: List<Base>, line: Line): Double {
    // TODO handle when base.quantity becomes < 0
    return bases
        .filter { base -> base.quantity >= line.quantityCurrency2 }
        .map { base ->
            base.quantity = base.quantity - line.quantityCurrency2
            if (base.quantity < 0) throw RuntimeException("base.quantity < 0. Not handled yet")
            if (line.currency1 == Currency.CARDANO && line.currency2 == Currency.ETH) {
                log.debug("${line.currency1}/${line.currency2} Original Price calculation: ${base.price} * ${line.quantityCurrency2}")
            }
            base.price * line.quantityCurrency2
        }
        .firstOrNull() ?: 0.0
}

class Base(val date: ZonedDateTime, val price: Double, var quantity: Double)
