package com.cryptax.controller

import com.cryptax.domain.entity.Currency
import io.reactivex.Single

class CurrencyController {
    fun getAllCurrencies(): Single<Array<Currency>> {
        return Single.just(Currency.values())
    }
}
