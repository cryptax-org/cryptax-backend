package com.cryptax.domain.entity

import java.time.ZonedDateTime

data class Report(
    val totalCapitalGainShort: Double,
    val totalCapitalGainLong: Double,
    val breakdown: Map<Currency, Details>)

data class Details(
    var capitalGainShort: Double = 0.0,
    var capitalGainLong: Double = 0.0,
    val lines: List<Line> = mutableListOf()) {

    constructor(line: Line) : this() {
        add(line)
    }

    fun add(line: Line): Details {
        (lines as MutableList).add(line)
        return this
    }

    fun sortByDate() {
        (lines as MutableList).sortWith(compareBy { it.date })
    }
}

data class Line(
    val transactionId: String,
    val date: ZonedDateTime,
    val currency1: Currency,
    val currency2: Currency,
    val type: Transaction.Type,
    val price: Double,
    val quantity: Double) {

    var metadata: Metadata = Metadata()

    fun currencies(): List<Currency> {
        return listOf(currency1, currency2)
    }

    fun deepCopy(): Line {
        val metadataCopy = metadata.copy()
        val lineCopy = this.copy()
        lineCopy.metadata = metadataCopy
        return lineCopy
    }
}

data class Metadata(var ignored: Boolean = true) {
    var currency1UsdValue: Double = 0.0
    var currency2UsdValue: Double = 0.0
    var quantityCurrency2: Double = 0.0 // quantity * price
    var capitalGainShort: Double = 0.0
    var capitalGainLong: Double = 0.0
}
