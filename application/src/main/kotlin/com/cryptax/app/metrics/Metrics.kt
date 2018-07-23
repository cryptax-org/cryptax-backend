package com.cryptax.app.metrics

import com.cryptax.app.routes.Routes.sendSuccess
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.dropwizard.MetricsService
import io.vertx.ext.web.Router

object Metrics {
    fun setupMetrics(metricsService: MetricsService, vertx: Vertx, router: Router) {
        router.get("/ping")
            .handler { routingContext ->
                sendSuccess(JsonObject().put("result", "pong"), routingContext.response())
            }

        router.get("/metrics/all")
            .handler { routingContext ->
                val metrics = metricsService.getMetricsSnapshot(vertx)
                sendSuccess(metrics, routingContext.response())
            }
    }
}
