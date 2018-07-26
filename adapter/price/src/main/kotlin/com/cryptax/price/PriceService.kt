package com.cryptax.price

import com.cryptax.domain.entity.Transaction
import com.cryptax.price.impl.CryptoCompare
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class PriceService(
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val api: CryptoApi = CryptoCompare(client, objectMapper)) : com.cryptax.domain.port.PriceService {

    companion object {
        private val log = LoggerFactory.getLogger(PriceService::class.java)
    }

    override fun getUsdAmount(transaction: Transaction): Double {
        val usdPrice = api.findUsdPriceAt(transaction.currency1, transaction.date)
        log.debug("Found for ${transaction.currency1.code} price of $usdPrice in USD at ${transaction.date}")
        return usdPrice * transaction.quantity
    }
}
