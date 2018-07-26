package com.cryptax.parser.model

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import java.time.ZonedDateTime

data class CoinbaseTransaction(
    val date: ZonedDateTime,
    val transactionType: String,
    val asset: Currency,
    val quantity: Double,
    val usdPrice: Double,
    val usdAmount: Double) {

    fun toTransaction(userId: String, source: Source): Transaction {
        return Transaction(
            userId = userId,
            source = source,
            date = date,
            type = Transaction.Type.valueOf(transactionType.toUpperCase()),
            price = usdPrice,
            quantity = quantity,
            currency1 = asset,
            currency2 = Currency.USD
        )
    }
}
