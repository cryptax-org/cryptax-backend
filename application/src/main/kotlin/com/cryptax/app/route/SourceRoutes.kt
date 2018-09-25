package com.cryptax.app.route

import com.cryptax.controller.SourceController
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class SourceRoutes @Autowired constructor(private val sourceController: SourceController) {

    @GetMapping("/sources")
    fun getAllCurrencies(): Single<List<String>> {
        return sourceController.getAllSources()
    }
}
