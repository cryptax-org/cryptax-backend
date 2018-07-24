package com.cryptax.app.routes

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.app.routes.Routes.sendSuccess
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.SortedMap

fun handleHealthRoutes(router: Router, vertxScheduler: Scheduler, healthCheckRegistry: HealthCheckRegistry) {
    router.get("/health")
        .handler { routingContext ->
            Single
                .create<SortedMap<String, HealthCheck.Result>> { emitter -> emitter.onSuccess(healthCheckRegistry.runHealthChecks()) }
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { result -> sendSuccess(JsonObject.mapFrom(result), routingContext.response()) },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(Failure.failureHandler)
}
