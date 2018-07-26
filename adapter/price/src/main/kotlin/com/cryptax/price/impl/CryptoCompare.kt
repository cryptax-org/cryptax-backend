package com.cryptax.price.impl

import com.cryptax.domain.entity.Currency
import com.cryptax.price.CryptoApi
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.ZonedDateTime

/**
 * https://min-api.cryptocompare.com
 */
class CryptoCompare : CryptoApi {


    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    override fun findUsdPriceAt(currency: Currency, date: ZonedDateTime): Double {
        val timestamp = Timestamp.from(date.toInstant())
        val request = Request.Builder().url("$BASE_URL/pricehistorical?fsym=${currency.code}&tsyms=USD&ts=${timestamp.time}").build()
        log.debug("Get ${currency.code} price in USD at $date ${request.url()}")
        val response = client.newCall(request).execute()
        val body = response.body()
        if (body == null) {
            throw RuntimeException("The body received was null")
        } else {
            val jsonResponse = objectMapper.readValue<JsonNode>(body.string(), JsonNode::class.java)
            return jsonResponse.get(currency.code).get("USD").toString().toDouble()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CryptoCompare::class.java)
        private const val BASE_URL = "https://min-api.cryptocompare.com/data"
    }
}
