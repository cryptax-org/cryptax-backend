package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.controller.ReportController
import com.cryptax.validation.RestValidation.generateReport
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleReportRoutes(router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler, reportController: ReportController) {

    // Generate a report for a user
    router.get("/users/:userId/report")
        .handler(jwtAuthHandler)
        .handler(generateReport)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val debug = routingContext.request().getParam("debug") ?: "false"
            reportController
                .generateReport(userId, debug.toBoolean())
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { report -> Routes.sendSuccess(JsonObject.mapFrom(report), routingContext.response()) },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)
}

