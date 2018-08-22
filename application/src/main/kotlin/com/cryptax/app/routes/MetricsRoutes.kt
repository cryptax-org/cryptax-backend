package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.dropwizard.MetricsService
import io.vertx.ext.web.Router

object MetricsRoutes {
    fun setupMetrics(metricsService: MetricsService, vertx: Vertx, router: Router) {
        router.get("/ping")
            .handler { routingContext ->
                sendSuccess(JsonObject().put("result", "pong"), routingContext.response())
            }
            .failureHandler(failureHandler)

        router.get("/metrics")
            .handler { routingContext ->
                val key = routingContext.request().getParam("key")
                val snapshot = metricsService.getMetricsSnapshot(vertx)
                val metrics = if (key == null) snapshot else snapshot.getJsonObject(key)
                sendSuccess(metrics, routingContext.response())
            }
            .failureHandler(failureHandler)

        router.get("/metrics/available")
            .handler { routingContext ->
                val available = JsonArray(metricsService.metricsNames().toList())
                sendSuccess(available, routingContext.response())
            }
            .failureHandler(failureHandler)
    }
}
