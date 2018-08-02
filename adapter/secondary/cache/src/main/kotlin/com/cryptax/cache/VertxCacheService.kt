package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.time.ZonedDateTime

private val log: Logger = LoggerFactory.getLogger(VertxCacheService::class.java)

class VertxCacheService(private val vertx: Vertx) : CacheService {

    override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
        log.trace("Cache access ${currency.code} $date")
        val jsonObject = vertx
            .sharedData()
            .getLocalMap<JsonObject, JsonObject>(name)[key(currency, date)] ?: return null
        return Pair(jsonObject.getString("service"), jsonObject.getDouble("value"))
    }

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {
        log.trace("Put in cache ${currency.code} $date $value")
        vertx
            .sharedData()
            .getLocalMap<JsonObject, JsonObject>(name)[key(currency, date)] = value(value)
    }

    private fun key(currency: Currency, date: ZonedDateTime): JsonObject {
        return JsonObject().put("currency", currency).put("date", date.toInstant().toEpochMilli() / 1000)
    }

    private fun value(value: Pair<String, Double>): JsonObject {
        return JsonObject().put("service", value.first).put("value", value.second)
    }
}
