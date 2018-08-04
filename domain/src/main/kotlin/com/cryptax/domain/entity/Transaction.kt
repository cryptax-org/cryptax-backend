package com.cryptax.domain.entity

import java.time.ZonedDateTime

data class Transaction(
    val id: String = "DEFAULT",
    val userId: String,
    val source: Source,
    val date: ZonedDateTime,
    val type: Type,
    val price: Double,
    val quantity: Double,
    val currency1: Currency,
    val currency2: Currency) {

    enum class Type {
        BUY, SELL
    }
}
