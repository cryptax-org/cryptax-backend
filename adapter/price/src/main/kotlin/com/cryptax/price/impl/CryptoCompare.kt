package com.cryptax.price.impl

import com.cryptax.domain.entity.Currency
import com.cryptax.price.CryptoApi
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

/**
 * https://min-api.cryptocompare.com
 */
class CryptoCompare(private val client: OkHttpClient = OkHttpClient(), private val objectMapper: ObjectMapper = ObjectMapper()) : CryptoApi {

    override fun findUsdPriceAt(currency: Currency, timestamp: Long): Pair<String, Double> {
        val request = Request.Builder().url("$BASE_URL/pricehistorical?fsym=${currency.code}&tsyms=USD&ts=$timestamp").build()
        log.debug("Get ${currency.code} price in USD ${request.url()}")
        val response = client.newCall(request).execute()
        val body = response.body()
        if (body == null) {
            throw RuntimeException("The body received was null")
        } else {
            val jsonResponse = objectMapper.readValue<JsonNode>(body.string(), JsonNode::class.java)
            if (!jsonResponse.has(currency.code)) {
                log.debug("Body received: $jsonResponse")
            }
            return Pair(NAME, jsonResponse.get(currency.code).get("USD").toString().toDouble())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CryptoCompare::class.java)
        private const val BASE_URL = "https://min-api.cryptocompare.com/data"
        private const val NAME = "cryptoCompare"
    }
}
