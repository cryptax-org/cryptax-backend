package com.cryptax.domain.port

import com.cryptax.domain.entity.Currency
import java.time.ZonedDateTime

interface PriceService {

    fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Pair<String, Double>
}
