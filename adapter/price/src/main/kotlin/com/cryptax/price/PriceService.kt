package com.cryptax.price

import com.cryptax.cache.CacheService
import com.cryptax.domain.entity.Currency
import com.cryptax.price.impl.CryptoCompare
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import java.time.ZonedDateTime

class PriceService(
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val cache: CacheService,
    private val api: CryptoApi = CryptoCompare(client, objectMapper)) : com.cryptax.domain.port.PriceService {

    companion object {
        private const val cacheName = "cache.currency"
    }

    override fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Pair<String, Double> {
        var value = cache.get(cacheName, currency, date)
        if (value == null) {
            value = api.findUsdPriceAt(currency, date.toInstant().toEpochMilli() / 1000)
            cache.put(cacheName, currency, date, value)
        }
        return value
    }
}
