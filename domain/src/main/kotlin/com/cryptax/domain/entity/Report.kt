package com.cryptax.domain.entity

data class FinalReport(
    val totalGainsLosses: Double,
    val breakdown: Map<Currency, Details>
)

data class Details(var gainsLosses: Double = 0.0, val lines: List<Line> = mutableListOf()) {
    fun add(line: Line): Details {
        (lines as MutableList).add(line)
        return this
    }

    fun sortByDate() {
        (lines as MutableList).sortWith(compareBy { it.date })
    }
}

data class Line(
    private val currency1UsdValue: Double,
    private val currency2UsdValue: Double,
    private val transaction: Transaction) : Comparable<Line> {

    val transactionId = transaction.id
    val date = transaction.date
    val currency1 = transaction.currency1
    val currency2 = transaction.currency2
    val type = transaction.type
    val price = transaction.price
    val quantity = transaction.quantity
    val metadata: Metadata = Metadata(
        currency1UsdValue = currency1UsdValue,
        currency2UsdValue = currency2UsdValue,
        quantityCurrency2 = transaction.quantity * transaction.price
    )

    fun currencies(): List<Currency> {
        return listOf(currency1, currency2)
    }

    override fun compareTo(other: Line): Int {
        return when {
        // FIXME
            transaction != other.transaction -> -1
            else -> 0
        }
    }
}

data class Metadata(
    var ignored: Boolean = true,
    val currency1UsdValue: Double,
    val currency2UsdValue: Double,
    var currentPrice: Double? = null,
    var originalPrice: Double? = null,
    var gainsLosses: Double? = null,
    val quantityCurrency2: Double
)
