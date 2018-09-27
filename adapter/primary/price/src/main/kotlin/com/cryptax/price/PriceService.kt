package com.cryptax.price

import com.cryptax.cache.CacheService
import com.cryptax.domain.entity.Currency
import com.cryptax.price.impl.CryptoCompare
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import okhttp3.OkHttpClient
import java.time.ZonedDateTime

class PriceService(
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val cache: CacheService,
    private val api: CryptoApi = CryptoCompare(client, objectMapper)) : com.cryptax.domain.port.PriceService {

    override fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Single<Pair<String, Double>> {
        return cache.get(cacheName, currency, date)
            .toSingle()
            .onErrorResumeNext {
                api.findUsdPriceAt(currency, date.toInstant().toEpochMilli() / 1000)
                    .flatMap { pair ->
                        cache.put(cacheName, currency, date, pair)
                            .map { _ -> pair }
                    }
            }
    }

    companion object {
        private const val cacheName = "cache.currency"
    }
}
