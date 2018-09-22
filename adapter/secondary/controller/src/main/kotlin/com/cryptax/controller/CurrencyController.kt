package com.cryptax.controller

import com.cryptax.controller.model.CurrencyWeb
import com.cryptax.domain.entity.Currency
import io.reactivex.Single

class CurrencyController {
    fun getAllCurrencies(): Single<List<CurrencyWeb>> {
        return Single.just(
            Currency.values()
                .filter { currency -> currency != Currency.UNKNOWN }
                .sortedBy { it.code }
                .map { currency ->
                    CurrencyWeb(
                        currency.code,
                        currency.fullName,
                        currency.symbol,
                        currency.type.name.toLowerCase())
                }
        )
    }
}
