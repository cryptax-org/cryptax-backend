package com.cryptax.price

import com.cryptax.domain.entity.Currency
import io.reactivex.Single

interface CryptoApi {

    fun findUsdPriceAt(currency: Currency, timestamp: Long): Single<Pair<String, Double>>
}
