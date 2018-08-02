package com.cryptax.parser.model

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.parser.utils.extractCurrencies
import java.time.ZonedDateTime

data class BinanceTransaction(
    val date: ZonedDateTime,
    val market: String,
    val type: String,
    val price: Double,
    val amount: Double,
    val total: Double,
    val fee: Double,
    val feeCoin: Currency) {

    fun toTransaction(userId: String, source: Source): Transaction {
        val market = extractCurrencies(market)
        return Transaction(
            userId = userId,
            source = source,
            date = date,
            type = Transaction.Type.valueOf(type),
            price = price,
            quantity = amount,
            currency1 = market.first,
            currency2 = market.second
        )
    }
}
