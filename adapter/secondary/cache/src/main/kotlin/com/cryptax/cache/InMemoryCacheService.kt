package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

// TODO: To implement
class InMemoryCacheService : CacheService {

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {
        log.trace("Put in cache ${currency.code} $date $value")
    }

    override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
        log.trace("Cache access ${currency.code} $date")
        return null
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(InMemoryCacheService::class.java)
    }
}
