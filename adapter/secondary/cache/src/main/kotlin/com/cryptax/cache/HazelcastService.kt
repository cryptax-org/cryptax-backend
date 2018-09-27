package com.cryptax.cache

import com.cryptax.cache.domain.Key
import com.cryptax.cache.domain.Value
import com.cryptax.domain.entity.Currency
import com.hazelcast.core.HazelcastInstance
import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class HazelcastService(private val hazelcast: HazelcastInstance) : CacheService {

    override fun get(name: String, currency: Currency, date: ZonedDateTime): Maybe<Pair<String, Double>> {
        return Maybe.create { emitter ->
            log.trace("Cache access ${currency.code} $date")
            val map = hazelcast.getMap<Key, Value>(name)
            val cache = map[Key(currency, date)]
            when (cache) {
                null -> emitter.onComplete()
                else -> emitter.onSuccess(Pair(cache.service, cache.value))
            }
        }
    }

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>): Single<Unit> {
        return Single.create { emitter ->
            log.trace("Put in cache ${currency.code} $date $value")
            val map = hazelcast.getMap<Key, Value>(name)
            map[Key(currency, date)] = Value(value.first, value.second)
            emitter.onSuccess(Unit)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HazelcastService::class.java)
    }
}
