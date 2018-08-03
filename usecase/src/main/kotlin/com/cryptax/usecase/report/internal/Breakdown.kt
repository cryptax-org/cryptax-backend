package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Details
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.ReportException
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class Breakdown(lines: List<Line>) : java.util.HashMap<Currency, Details>() {
    init {
        lines.map { line ->
            line.currencies()
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .forEach { currency ->
                    val lineCopy = line.deepCopy()
                    val details = this[currency]
                    if (details != null) {
                        details.add(lineCopy)
                    } else {
                        this[currency] = Details(lineCopy)
                    }
                }
        }
        this.values.forEach { list -> list.sortByDate() }
    }

    val linesToCompute: List<Line> by lazy { linesToComputeLazy() }
    var totalCapitalGainShort = 0.0
        private set
    var totalCapitalGainLong = 0.0
        private set

    fun compute() {
        log.info("Compute report...")
        this.values
            .flatMap { detail -> detail.lines }
            .filter { line -> !line.metadata.ignored }
            .forEach { line ->
                line.currencies()
                    .filter { currency -> currency.type == Currency.Type.CRYPTO }
                    .forEach { currency: Currency ->
                        val priceUsdAtSellDate = getPriceUsdAtSaleDate(line)
                        val ownedCoins: List<OwnedCoins> = extractCoinsOwned(currency, lines(currency))
                        if (ownedCoins.isNotEmpty()) {
                            val capitalGain = getCapitalGain(ownedCoins, line, priceUsdAtSellDate)
                            line.metadata.ignored = false
                            line.metadata.capitalGainShort = capitalGain.first
                            line.metadata.capitalGainLong = capitalGain.second
                        }
                        computeCapitalGainForOneCurrency(currency)
                    }
            }
        computerTotalCapitalGain()
    }

    private fun computeCapitalGainForOneCurrency(currency: Currency) {
        // Compute capital gain for one currency
        details(currency).capitalGainShort = lines(currency)
            .map { line -> line.metadata.capitalGainShort }
            .sum()
        details(currency).capitalGainLong = lines(currency)
            .map { line -> line.metadata.capitalGainLong }
            .sum()
    }

    private fun computerTotalCapitalGain() {
        totalCapitalGainShort = keys
            .filter { currency -> currency.type == Currency.Type.CRYPTO }
            .map { currency -> details(currency).capitalGainShort }
            .sum()
        totalCapitalGainLong = keys
            .filter { currency -> currency.type == Currency.Type.CRYPTO }
            .map { currency -> details(currency).capitalGainLong }
            .sum()
    }

    private fun linesToComputeLazy(): List<Line> {
        val result = ArrayList<Line>()
        for (currency in this.keys.filter { currency -> currency.type == Currency.Type.CRYPTO }) {
            val lines = lines(currency)
            val ownedCoins: List<OwnedCoins> = extractCoinsOwned(currency, lines)
            if (ownedCoins.isNotEmpty()) {
                result.addAll(
                    lines.filter { line ->
                        (line.currency2 == currency && line.type == Transaction.Type.BUY)
                            || (line.currency1 == currency && line.type == Transaction.Type.SELL)
                    }
                )
            }
        }
        return result
    }

    private fun getPriceUsdAtSaleDate(line: Line): Double {
        return if (line.type == Transaction.Type.BUY) {
            line.metadata.currency2UsdValue
        } else {
            line.price
        }
    }

    private fun extractCoinsOwned(currency: Currency, lines: List<Line>): List<OwnedCoins> {
        return lines
            .filter { line -> line.currency1 == currency && line.type == Transaction.Type.BUY }
            .map { line -> OwnedCoins(line.date, line.price, line.quantity) }
    }

    /**
     * Pair<Short gain, Long gain>
     */
    private fun getCapitalGain(ownedCoins: List<OwnedCoins>, line: Line, sellPrice: Double): Pair<Double, Double> {
        val quantity = if (line.type == Transaction.Type.BUY) line.metadata.quantityCurrency2 else line.quantity
        val mutablePair = getCapitalGain(ownedCoins, 0, sellPrice, quantity, line.date)
        return Pair(mutablePair.first, mutablePair.second)
    }

    private fun getCapitalGain(ownedCoins: List<OwnedCoins>, index: Int, sellPrice: Double, quantity: Double, date: ZonedDateTime): MutablePair<Double, Double> {
        val coin = ownedCoins[index]
        return when {
            coin.quantity >= quantity -> {
                coin.quantity = coin.quantity - quantity
                return if (isShortCapitalGain(coin.date, date)) {
                    MutablePair((sellPrice * quantity) - (coin.price * quantity), 0.0)
                } else {
                    MutablePair(0.0, (sellPrice * quantity) - (coin.price * quantity))
                }
            }
            index < ownedCoins.size - 1 -> {
                val capitalGain = sellPrice - (coin.price * coin.quantity)
                val rest = quantity - coin.quantity
                coin.quantity = 0.0
                val result = getCapitalGain(ownedCoins, index + 1, sellPrice, rest, date)
                if (isShortCapitalGain(coin.date, date)) {
                    result.first += capitalGain
                } else {
                    result.second += capitalGain
                }
                result
            }
            else -> throw ReportException("Not enough coins: $ownedCoins")
        }
    }

    private fun isShortCapitalGain(first: ZonedDateTime, second: ZonedDateTime): Boolean {
        return ChronoUnit.YEARS.between(first, second) < 1L
    }

    internal fun details(currency: Currency): Details {
        val details = this[currency]
        if (details != null) {
            return details
        } else {
            throw ReportException("Could not find [${currency.code}]")
        }
    }

    internal fun lines(currency: Currency): List<Line> {
        return details(currency).lines
    }

    companion object {
        private val log = LoggerFactory.getLogger(Breakdown::class.java)
    }
}

private data class OwnedCoins(val date: ZonedDateTime, val price: Double, var quantity: Double)

private data class MutablePair<A, B>(var first: A, var second: B)
