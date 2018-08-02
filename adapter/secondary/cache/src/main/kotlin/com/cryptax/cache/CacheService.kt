package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import java.time.ZonedDateTime

interface CacheService {

    fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>?

    fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>)
}
