package com.cryptax.domain.entity

import java.util.TreeSet

data class Report(
    // FIXME string should be currency
    val pairs: Map<String, Result> = HashMap()
)

data class Line(
    val amountSource: String? = null,
    val usdCurrency1: Double,
    val usdCurrency2: Double,
    val transaction: Transaction) : Comparable<Line> {

    override fun compareTo(other: Line): Int {
        return when {
            usdCurrency1 != other.usdCurrency1 -> -1
            usdCurrency2 != other.usdCurrency2 -> -1
            amountSource != other.amountSource -> -1
            transaction != other.transaction -> -1
            else -> 0
        }
    }
}

data class Result(var gain: Double = 0.0, val lines: TreeSet<Line> = TreeSet(Comparator { o1, o2 -> o1.transaction.date.compareTo(o2.transaction.date) })) {
    fun add(line: Line): Result {
        lines.add(line)
        return this
    }
}
