package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import io.reactivex.Maybe
import io.reactivex.Single
import java.time.ZonedDateTime

interface CacheService {

    fun get(name: String, currency: Currency, date: ZonedDateTime): Maybe<Pair<String, Double>>

    fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>): Single<Unit>
}
