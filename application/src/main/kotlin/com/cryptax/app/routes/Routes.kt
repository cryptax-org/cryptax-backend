package com.cryptax.app.routes

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.config.AppConfig
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.LoggerFormat
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl
import io.vertx.reactivex.RxHelper

val bodyHandler: BodyHandler = BodyHandler.create()

object Routes {

    private val log = LoggerFactory.getLogger(Routes::class.java)

    fun setupRoutes(appConfig: AppConfig,
                    vertx: Vertx, router: Router,
                    userController: UserController,
                    transactionController: TransactionController,
                    healthCheckRegistry: HealthCheckRegistry) {

        val jwtProvider = JWTAuth.create(vertx, appConfig.jwtAuthOptions)
        val jwtAuthHandler = JWTAuthHandlerCustom(jwtProvider)
        val jwtRefreshAuthHandler = JWTRefreshAuthHandlerCustom(jwtProvider)
        val vertxScheduler = RxHelper.scheduler(vertx)

        router.route().handler(LoggerHandlerImpl(LoggerFormat.SHORT))
        handleUserRoutes(router, jwtAuthHandler, vertxScheduler, userController)
        handleTokenRoutes(appConfig, router, jwtProvider, jwtRefreshAuthHandler, vertxScheduler, userController)
        handleTransactionRoutes(router, jwtAuthHandler, vertxScheduler, transactionController)
        handleHealthRoutes(router, vertxScheduler, healthCheckRegistry)

        // Exception handler
        router.exceptionHandler { throwable ->
            log.error("Unrecoverable exception while processing a request", throwable)
        }
    }

    fun sendSuccess(body: JsonObject, response: HttpServerResponse) {
        response
            .addContentTypeJson()
            .end(body.encodePrettily())
    }

    fun sendSuccess(body: JsonArray, response: HttpServerResponse) {
        response
            .addContentTypeJson()
            .end(body.encodePrettily())
    }

    fun sendError(statusCode: Int, response: HttpServerResponse) {
        response
            .addContentTypeJson()
            .setStatusCode(statusCode)
            .end()
    }

    fun HttpServerResponse.addContentTypeJson(): HttpServerResponse {
        this.putHeader("content-type", "application/json")
        return this
    }
}
