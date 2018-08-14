package com.cryptax.app.metrics

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.dropwizard.MetricsService
import io.vertx.ext.web.Router

object Metrics {
    fun setupMetrics(metricsService: MetricsService, vertx: Vertx, router: Router) {
        router.get("/")
            .handler { routingContext -> routingContext.response().end() }
            .failureHandler(failureHandler)

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
