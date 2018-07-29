package com.cryptax.domain.entity

data class FinalReport(
    val gainsLoses: Double,
    val result: Map<String, Lines>
)

data class Line(
    var ignored: Boolean = true,
    val currency1UsdValue: Double,
    val currency2UsdValue: Double,

    var currentPrice: Double? = null,
    var originalPrice: Double? = null,
    var gainsLosses: Double? = null,
    private val transaction: Transaction) : Comparable<Line> {

    val date = transaction.date
    val currency1 = transaction.currency1
    val currency2 = transaction.currency2
    val type = transaction.type
    val price = transaction.price
    val quantity = transaction.quantity
    val quantityCurrency2 = transaction.quantity * transaction.price

    fun currencies(): List<Currency> {
        return listOf(currency1, currency2)
    }

    override fun compareTo(other: Line): Int {
        return when {
            currency1UsdValue != other.currency1UsdValue -> -1
            currency2UsdValue != other.currency2UsdValue -> -1
            transaction != other.transaction -> -1
            else -> 0
        }
    }
}

data class Lines(var gainsLosses: Double? = null, val lines: List<Line> = mutableListOf()) {
    fun add(line: Line): Lines {
        (lines as MutableList).add(line)
        return this
    }

    fun sortByDate() {
        (lines as MutableList).sortWith(compareBy { it.date })
    }
}
