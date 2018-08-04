package com.cryptax.app.verticle

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.app.metrics.Metrics
import com.cryptax.app.routes.Routes
import com.cryptax.config.AppConfig
import com.cryptax.controller.ReportController
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.dropwizard.MetricsService
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

private val log: Logger = LoggerFactory.getLogger(RestVerticle::class.java)

class RestVerticle(private val appConfig: AppConfig, kodein: Kodein) : AbstractVerticle() {

    private val userController by kodein.instance<UserController>()
    private val transactionController by kodein.instance<TransactionController>()
    private val reportController by kodein.instance<ReportController>()
    private val healthCheckRegistry by kodein.instance<HealthCheckRegistry>()
    private val objectMapper by kodein.instance<ObjectMapper>()

    override fun start(startFuture: Future<Void>) {
        Json.mapper = objectMapper
        val metricsService = MetricsService.create(vertx)

        // Create router
        val router = Router.router(vertx)
        Routes.setupRoutes(appConfig, vertx, router, userController, transactionController, reportController, healthCheckRegistry)
        Metrics.setupMetrics(metricsService, vertx, router)
        router.route().handler(CorsHandler.create("*").allowedMethods(setOf(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)))

        // Server options
        val options = HttpServerOptions()
        options.logActivity = true

        val port = appConfig.properties.server.port
        // Create server
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(port, appConfig.properties.server.domain) { ar ->
            if (ar.failed()) {
                log.error("Failed to deploy ${this.javaClass.simpleName}", ar.cause())
                startFuture.fail(ar.cause())
            } else {
                log.info("Server starter with profile [${appConfig.profile}] and listening on port [$port]")
                startFuture.complete()
            }
        }
    }
}
