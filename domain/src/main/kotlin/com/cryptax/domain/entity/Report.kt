package com.cryptax.domain.entity

data class Report(
    val lines: MutableList<Line> = mutableListOf()
)

data class Line(
    val transaction: Transaction,
    val usdAmount: Double
)
