package com.cryptax.price

import com.cryptax.domain.entity.Currency
import java.time.ZonedDateTime

interface CryptoApi {

    fun findUsdPriceAt(currency1: Currency, currency2: Currency, date: ZonedDateTime): Triple<String?, Double, Double>
}
