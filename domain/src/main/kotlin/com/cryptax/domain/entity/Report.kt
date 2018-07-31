package com.cryptax.domain.entity

import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(Report::class.java.simpleName)

data class Report(
    val totalCapitalGainShort: Double,
    val totalCapitalGainLong: Double,
    val breakdown: Breakdown)

class Breakdown(lines: List<Line>) : java.util.HashMap<Currency, Details>() {
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
                line.metadata.ignored = false
                line.metadata.priceUsdAtSellDate = priceUsdAtSellDate
                line.metadata.capitalGainShort = getCapitalGainShort(ownedCoins, line, priceUsdAtSellDate)
                // FIXME: This need to compute long/short gains
                line.metadata.capitalGainLong = 0.0
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

    private fun getCapitalGainShort(ownedCoins: List<OwnedCoins>, line: Line, sellPrice: Double): Double {
        return getCapitalGainShort(ownedCoins, 0, sellPrice, line.metadata.quantityCurrency2)
    }

    private fun getCapitalGainShort(ownedCoins: List<OwnedCoins>, index: Int, sellPrice: Double, quantity: Double): Double {
        val coin = ownedCoins[index]
        return if (coin.quantity >= quantity) {
            coin.quantity = coin.quantity - quantity
            // capital gain
            sellPrice - (coin.price * quantity)
        } else {
            if (index < ownedCoins.size - 1) {
                val capitalGain = sellPrice - (coin.price * coin.quantity)
                val rest = quantity - coin.quantity
                coin.quantity = 0.0
                capitalGain + getCapitalGainShort(ownedCoins, index + 1, sellPrice, rest)
            } else {
                throw RuntimeException("Not enough coins: $ownedCoins")
            }
        }
    }
}

data class Details(private val line: Line) {
    var capitalGainShort: Double = 0.0
    var capitalGainLong: Double = 0.0
    val lines: MutableList<Line> = mutableListOf()

    init {
        add(line)
    }

    fun add(line: Line): Details {
        lines.add(line)
        return this
    }

    fun sortByDate() {
        lines.sortWith(compareBy { it.date })
    }
}

data class Line(
    val transactionId: String,
    val date: ZonedDateTime,
    val currency1: Currency,
    val currency2: Currency,
    val type: Transaction.Type,
    val price: Double,
    val quantity: Double,
    private val currency1UsdValue: Double,
    private val currency2UsdValue: Double) {

    val metadata: Metadata = Metadata(
        currency1UsdValue = currency1UsdValue,
        currency2UsdValue = currency2UsdValue,
        quantityCurrency2 = quantity * price)

    fun currencies(): List<Currency> {
        return listOf(currency1, currency2)
    }
}

data class Metadata(
    var ignored: Boolean = true,
    val currency1UsdValue: Double,
    val currency2UsdValue: Double,
    var priceUsdAtSellDate: Double? = null,
    var capitalGainShort: Double? = null,
    var capitalGainLong: Double? = null,
    val quantityCurrency2: Double
)

data class OwnedCoins(val date: ZonedDateTime, val price: Double, var quantity: Double)
