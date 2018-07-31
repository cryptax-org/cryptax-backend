package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Details
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Transaction
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

internal class Breakdown(lines: List<Line>) : java.util.HashMap<Currency, Details>() {
    init {
        lines.map { line ->
            line.currencies()
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .forEach { currency ->
                    val lineCopy = line.copy()
                    if (this.containsKey(currency)) {
                        this[currency]!!.add(lineCopy)
                    } else {
                        this[currency] = Details(lineCopy)
                    }
                }
        }
        this.values.forEach { list -> list.sortByDate() }
    }

    fun compute() {
        log.info("Compute report...")
        // Loop over all Crypto Currencies
        for (currency in this.keys.filter { currency -> currency.type == Currency.Type.CRYPTO }) {
            // For each Crypto Currency extract how many coins are owned (has been bought)
            val lines = this[currency]!!.lines
            val ownedCoins: List<OwnedCoins> = extractCoinsOwned(currency, lines)

            // For each line (that match the filter) compute short and long capital gain
            for (line in lines.filter { line -> line.currency2 == currency && line.type == Transaction.Type.BUY }) {
                val priceUsdAtSellDate = line.metadata.currency2UsdValue * line.quantity * line.price
                val capitalGain = getCapitalGain(ownedCoins, line, priceUsdAtSellDate)
                line.metadata.ignored = false
                line.metadata.priceUsdAtSellDate = priceUsdAtSellDate
                line.metadata.capitalGainShort = capitalGain.first
                line.metadata.capitalGainLong = capitalGain.second
            }
            // Compute capital gain for each currency
            this[currency]!!.capitalGainShort = lines
                .map { line -> line.metadata.capitalGainShort ?: 0.0 }
                .sum()
            this[currency]!!.capitalGainLong = lines
                .map { line -> line.metadata.capitalGainLong ?: 0.0 }
                .sum()
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
        val mutablePair = getCapitalGain(ownedCoins, 0, sellPrice, line.metadata.quantityCurrency2, line.date, MutablePair(0.0, 0.0))
        return Pair(mutablePair.first, mutablePair.second)
    }

    private fun getCapitalGain(ownedCoins: List<OwnedCoins>, index: Int, sellPrice: Double, quantity: Double, buyDate: ZonedDateTime, result: MutablePair<Double, Double>): MutablePair<Double, Double> {
        val coin = ownedCoins[index]
        return if (coin.quantity >= quantity) {
            coin.quantity = coin.quantity - quantity
            // capital gain
            if (isShortCapitalGain(coin.date, buyDate)) {
                result.first = result.first + (sellPrice - (coin.price * quantity))
            } else {
                result.second = result.second + (sellPrice - (coin.price * quantity))
            }
            result
        } else {
            if (index < ownedCoins.size - 1) {
                val capitalGain = sellPrice - (coin.price * coin.quantity)
                val rest = quantity - coin.quantity
                coin.quantity = 0.0
                if (isShortCapitalGain(coin.date, buyDate)) {
                    result.first = result.first + capitalGain + getCapitalGain(ownedCoins, index + 1, sellPrice, rest, buyDate, result).first
                } else {
                    result.second = result.second + capitalGain + getCapitalGain(ownedCoins, index + 1, sellPrice, rest, buyDate, result).second
                }
                result
            } else {
                throw RuntimeException("Not enough coins: $ownedCoins")
            }
        }
    }

    private fun isShortCapitalGain(sellDate: ZonedDateTime, buyDate: ZonedDateTime): Boolean {
        return buyDate.plusYears(1).isAfter(sellDate)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Breakdown::class.java.simpleName)
    }
}

internal data class OwnedCoins(val date: ZonedDateTime, val price: Double, var quantity: Double)

private data class MutablePair<A, B>(var first: A, var second: B)
