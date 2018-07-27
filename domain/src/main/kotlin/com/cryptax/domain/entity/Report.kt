package com.cryptax.domain.entity

import java.util.TreeSet

data class Report(
    val pairs: Map<String, TreeSet<Line>> = HashMap()
)

data class Line(
    val usdAmount: Double,
    val amountSource: String? = null,
    val transaction: Transaction) : Comparable<Line> {

    override fun compareTo(other: Line): Int {
        return when {
            usdAmount != other.usdAmount -> -1
            amountSource != other.amountSource -> -1
            transaction != other.transaction -> -1
            else -> 0
        }
    }
}
