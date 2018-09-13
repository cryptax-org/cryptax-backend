package com.cryptax.app.route

import com.cryptax.controller.CurrencyController
import com.cryptax.domain.entity.Currency
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.reactivex.Observable
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyRoutes @Autowired constructor(private val currencyController: CurrencyController) {

    @GetMapping("/currencies")
    fun getAllCurrencies(): Single<List<JsonNode>> {
        return currencyController
            .getAllCurrencies()
            .flatMapObservable { array ->
                Observable.create<Currency> { emitter ->
                    array.forEach { emitter.onNext(it) }
                    emitter.onComplete()
                }
            }
            .filter { currency -> currency != Currency.UNKNOWN }
            .sorted { c1: Currency, c2: Currency -> c1.code.compareTo(c2.code) }
            .map { currency ->
                JsonNodeFactory.instance.objectNode()
                    .put("code", currency.code)
                    .put("name", currency.fullName)
                    .put("symbol", currency.symbol)
                    .put("type", currency.type.name.toLowerCase())
            }
            .collectInto(mutableListOf()) { result: List<JsonNode>, json: JsonNode -> (result as MutableList).add(json) }
    }
}
