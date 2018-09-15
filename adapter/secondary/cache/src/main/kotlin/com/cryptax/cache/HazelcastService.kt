package com.cryptax.cache

import com.cryptax.cache.domain.Key
import com.cryptax.cache.domain.Value
import com.cryptax.domain.entity.Currency
import com.hazelcast.core.HazelcastInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class HazelcastService(private val hazelcast: HazelcastInstance) : CacheService {

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {
        log.trace("Put in cache ${currency.code} $date $value")
        val map = hazelcast.getMap<Key, Value>(name)
        map[Key(currency, date)] = Value(value.first, value.second)
    }

    override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
        log.trace("Cache access ${currency.code} $date")
        val map = hazelcast.getMap<Key, Value>(name)
        val cache = map[Key(currency, date)] ?: return null
        return Pair(cache.service, cache.value)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HazelcastService::class.java)
    }
}
