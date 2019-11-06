package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.security.SecurityContext
import com.cryptax.controller.ReportController
import com.cryptax.controller.model.ReportWeb
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.reactivex.Single

@Controller
class ReportRoutes(private val reportController: ReportController, private val service: SecurityContext) {

    @Get("/users/{userId}/report")
    fun generateReport(
        @PathVariable userId: String,
        @QueryValue(value = "debug", defaultValue = "false") debug: Boolean): Single<ReportWeb> {
        return service.validateUserId(userId).flatMap { reportController.generateReport(userId, debug) }
    }
}
