package com.cryptax.app.verticle

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.app.routes.MetricsRoutes
import com.cryptax.app.routes.Routes
import com.cryptax.config.Config
import com.cryptax.controller.CurrencyController
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

class RestVerticle(private val config: Config, kodein: Kodein) : AbstractVerticle() {

    private val userController by kodein.instance<UserController>()
    private val transactionController by kodein.instance<TransactionController>()
    private val reportController by kodein.instance<ReportController>()
    private val healthCheckRegistry by kodein.instance<HealthCheckRegistry>()
    private val currencyController by kodein.instance<CurrencyController>()
    private val objectMapper by kodein.instance<ObjectMapper>()

    override fun start(startFuture: Future<Void>) {
        Json.mapper = objectMapper
        val metricsService = MetricsService.create(vertx)

        // Create router
        val router = Router.router(vertx)
        router.route().handler(
            CorsHandler.create(config.properties.server.allowOrigin)
                .allowedMethods(setOf(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS))
                .allowedHeaders(setOf("Accept", "Content-Type", "Authorization")))
        Routes.setupRoutes(config, vertx, router, userController, transactionController, reportController, currencyController, healthCheckRegistry)
        MetricsRoutes.setupMetrics(metricsService, vertx, router)

        // Server options
        val options = HttpServerOptions()
        options.logActivity = true

        val port = config.properties.server.port
        // Create server
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(port, config.properties.server.domain) { ar ->
            if (ar.failed()) {
                log.error("Failed to deploy ${this.javaClass.simpleName}", ar.cause())
                startFuture.fail(ar.cause())
            } else {
                log.info("Server starter with profile [${config.profile}] and listening on port [$port]")
                startFuture.complete()
            }
        }
    }
}
