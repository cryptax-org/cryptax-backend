package com.cryptax.price

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Transaction
import com.cryptax.price.impl.CryptoCompare
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class PriceService(
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val api: CryptoApi = CryptoCompare(client, objectMapper)) : com.cryptax.domain.port.PriceService {

    companion object {
        private val log = LoggerFactory.getLogger(PriceService::class.java)
    }

    override fun getUsdAmount(transaction: Transaction): Triple<String?, Double, Double> {
        val usd = api.findUsdPriceAt(transaction.currency1, transaction.currency2, transaction.date)
        log.debug("Found for ${transaction.currency1.code} price of ${usd.second} in USD at ${transaction.date}")
        log.debug("Found for ${transaction.currency2.code} price of ${usd.third} in USD at ${transaction.date}")
        return Triple(usd.first, usd.second * transaction.quantity, usd.third * transaction.quantity * transaction.price)
    }

    override fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Double {
        val timestamp = date.toInstant().toEpochMilli() / 1000
        return api.findUsdPriceAt(currency, timestamp).second
    }
}
