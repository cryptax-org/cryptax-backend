package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import java.time.ZonedDateTime

// TODO: To implement
class InMemoryCacheService : CacheService {

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {
    }

    override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
        return null
    }
}
