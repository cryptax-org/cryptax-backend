package com.cryptax.domain.port

import com.cryptax.domain.entity.Currency
import io.reactivex.Single
import java.time.ZonedDateTime

interface PriceService {

    fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Single<Pair<String, Double>>
}
