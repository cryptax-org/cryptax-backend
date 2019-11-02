package com.cryptax.app.micronaut.route

import com.cryptax.controller.CurrencyController
import com.cryptax.controller.model.CurrencyWeb
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single

@Controller
class CurrencyRoutes constructor(private val currencyController: CurrencyController) {

    @Get("/currencies")
    fun getAllCurrencies(): Single<List<CurrencyWeb>> {
        return currencyController.getAllCurrencies()
    }
}
