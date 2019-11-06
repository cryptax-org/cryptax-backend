package com.cryptax.app.route

import com.cryptax.controller.CurrencyController
import com.cryptax.controller.model.CurrencyWeb
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyRoutes @Autowired constructor(private val currencyController: CurrencyController) {

    @GetMapping("/currencies")
    fun getAllCurrencies(): Single<List<CurrencyWeb>> {
        return currencyController.getAllCurrencies()
    }
}
