package com.cryptax.parser

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.Transaction.Type
import java.time.ZonedDateTime

data class TransactionParser(
    val source: Source,
    val date: ZonedDateTime,
    val type: Type,
    val price: Double,
    val amount: Double,
    val currency1: Currency,
    val currency2: Currency) {

    fun toTransaction(userId: String): Transaction {
        return Transaction(
            userId = userId,
            source = source,
            date = date,
            type = type,
            price = price,
            amount = amount,
            currency1 = currency1,
            currency2 = currency2
        )
    }
}
