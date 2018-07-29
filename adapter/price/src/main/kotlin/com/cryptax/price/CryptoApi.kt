package com.cryptax.price

import com.cryptax.domain.entity.Currency

interface CryptoApi {

    fun findUsdPriceAt(currency: Currency, timestamp: Long): Pair<String, Double>
}
