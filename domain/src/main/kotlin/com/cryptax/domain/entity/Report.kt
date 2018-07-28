package com.cryptax.domain.entity

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.TreeSet

data class Report(
    // FIXME string should be currency
    val pairs: Map<String, Result> = HashMap()
)

data class Line(
    val currency1UsdValue: Double,
    val currency2UsdValue: Double,
    var gain: Double,
    val transaction: Transaction) : Comparable<Line> {

    override fun compareTo(other: Line): Int {
        return when {
            currency1UsdValue != other.currency1UsdValue -> -1
            currency2UsdValue != other.currency2UsdValue -> -1
            transaction != other.transaction -> -1
            else -> 0
        }
    }
}

data class Result(private val currency: Currency, val lines: TreeSet<Line> = TreeSet(Comparator { o1, o2 -> o1.transaction.date.compareTo(o2.transaction.date) })) {

    var gain = 0.0

    // Date / quantity / currency price at the time
    private var stateQuantityPrice: MutableList<MutableTriple<ZonedDateTime, Double, Double>> = mutableListOf()

    fun add(line: Line): Result {
        if (line.transaction.currency1.code == currency.code && line.transaction.type == Transaction.Type.BUY) {
            stateQuantityPrice.add(MutableTriple(line.transaction.date, line.transaction.quantity, line.currency1UsdValue))
        }

        lines.add(line)
        return this
    }

    fun computeGain() {
        lines
            .filter { line -> line.transaction.currency2.code == currency.code && line.transaction.type == Transaction.Type.BUY }
            .forEach { line ->
                val currentPrice = line.currency2UsdValue * line.transaction.quantity * line.transaction.price
                val originalPrice = getOriginalPrice(line)
                val gainDiff = currentPrice - originalPrice
                log.debug("${line.transaction.currency1.code}/${line.transaction.currency2.code} Current: $currentPrice Original: $originalPrice Gain: $gainDiff")
                //gain += gainDiff
                line.gain = gainDiff
                this.gain += gainDiff
            }
    }

    private fun getOriginalPrice(line: Line): Double {
        for (triple in stateQuantityPrice) {
            if (triple.second <= line.transaction.quantity) {
                triple.second = triple.second - line.transaction.quantity
                return triple.third * line.transaction.quantity * line.transaction.price
            }
        }
        throw RuntimeException("Not enough money I guess")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Result::class.java)
    }
}

private data class MutableTriple<A, B, C>(
    var first: A,
    var second: B,
    var third: C)
