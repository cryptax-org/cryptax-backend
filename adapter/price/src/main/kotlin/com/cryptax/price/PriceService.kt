package com.cryptax.price

import com.cryptax.domain.entity.Transaction
import com.cryptax.price.impl.CryptoCompare
import org.slf4j.LoggerFactory

class PriceService(private val api: CryptoApi = CryptoCompare()) : com.cryptax.domain.port.PriceService {

    companion object {
        private val log = LoggerFactory.getLogger(PriceService::class.java)
    }

    override fun getPriceInDollars(transaction: Transaction): Double {
        val priceInDollars = api.getCurrencyInDollarsAt(transaction.currency1, transaction.date)
        log.debug("Found for ${transaction.currency1.code} price of $priceInDollars in USD")
        return priceInDollars * transaction.quantity
    }
}
