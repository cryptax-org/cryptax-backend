package com.cryptax.domain.port

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Transaction
import java.time.ZonedDateTime

interface PriceService {

    fun getUsdAmount(transaction: Transaction): Triple<String?, Double, Double>

    fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Double
}
