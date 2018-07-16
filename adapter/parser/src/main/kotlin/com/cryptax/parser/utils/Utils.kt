package com.cryptax.parser.utils

import com.cryptax.domain.entity.Currency

fun extractCurrencies(market: String): Pair<Currency, Currency> {
    if (market.length <= 5) {
        throw RuntimeException("[$market] should be at least >= 6")
    }
    var currency1 = Currency.UNKNOWN
    val currency2: Currency
    if (market.length % 2 == 0) {
        currency1 = Currency.findCurrency(market.substring(0, market.length / 2))
        currency2 = Currency.findCurrency(market.substring(market.length / 2, market.length))
    } else {
        var start = (market.length / 2) - 1
        while (currency1 == Currency.UNKNOWN && start < market.length) {
            currency1 = Currency.findCurrency(market.substring(0, start))
            start++
        }
        currency2 = Currency.findCurrency(market.substring(start - 1, market.length))
    }
    if (currency1 == Currency.UNKNOWN || currency2 == Currency.UNKNOWN) {
        throw RuntimeException("Was not able to find currency in [$market]")
    }
    return Pair(currency1, currency2)
}
