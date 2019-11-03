package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.security.SecurityContext
import com.cryptax.controller.CurrencyController
import com.cryptax.controller.model.CurrencyWeb
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single

@Controller
class CurrencyRoutes constructor(
    private val currencyController: CurrencyController,
    private val securityContext: SecurityContext) {

    @Get("/currencies")
    fun getAllCurrencies(): Single<List<CurrencyWeb>> {
        return securityContext.validateRequest().flatMap { currencyController.getAllCurrencies() }
    }
}
