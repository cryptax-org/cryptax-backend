package com.cryptax.price

import com.cryptax.domain.entity.Currency
import java.time.ZonedDateTime

interface CryptoApi {

    fun findUsdPriceAt(currency: Currency, date: ZonedDateTime): Double
}
