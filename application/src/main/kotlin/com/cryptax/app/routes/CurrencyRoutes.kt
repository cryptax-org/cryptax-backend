package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.controller.CurrencyController
import com.cryptax.domain.entity.Currency
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleCurrenciesRoutes(router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler, currencyController: CurrencyController) {
    router.get("/currencies")
        .handler(jwtAuthHandler)
        .handler { routingContext ->
            currencyController
                .getAllCurrencies()
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { currencies ->
                        run {
                            val result = currencies
                                .filter { currency -> currency != Currency.UNKNOWN }
                                .sortedBy { currency -> currency.code }
                                .map { currency -> JsonObject().put("code", currency.code).put("name", currency.fullName).put("symbol", currency.symbol).put("type", currency.type.name.toLowerCase()) }
                            sendSuccess(JsonArray(result), routingContext.response())
                        }
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)
}
