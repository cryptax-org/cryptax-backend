package com.cryptax.app.micronaut.route

import com.cryptax.controller.SourceController
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single

@Controller
class SourceRoutes(private val sourceController: SourceController) {

    @Get("/sources")
    fun getAllCurrencies(): Single<List<String>> {
        return sourceController.getAllSources()
    }
}
