package com.cryptax.app.route

import com.cryptax.controller.ReportController
import com.cryptax.controller.model.ReportWeb
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import reactor.adapter.rxjava.toMono
import reactor.core.publisher.Mono

@Controller
class ReportRoutes(private val reportController: ReportController) {

    @Get("/users/{userId}/report")
    fun generateReport(
        @PathVariable userId: String,
        @QueryValue(value = "debug", defaultValue = "false") debug: Boolean): Mono<ReportWeb> {
        return verifyUserId(userId).flatMap { reportController.generateReport(userId, debug).toMono() }
    }
}
