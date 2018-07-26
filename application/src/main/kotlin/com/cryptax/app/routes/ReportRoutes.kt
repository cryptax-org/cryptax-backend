package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.controller.ReportController
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleReportRoutes(router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler, reportController: ReportController) {

    // Generate a report for a user
    router.get("/users/:userId/report")
        .handler(jwtAuthHandler)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            reportController
                .generateReport(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { report -> Routes.sendSuccess(JsonObject.mapFrom(report), routingContext.response()) },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)
}

