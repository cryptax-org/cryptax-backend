package com.cryptax.app.routes

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.config.Config
import com.cryptax.controller.CurrencyController
import com.cryptax.controller.ReportController
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.LoggerFormat
import io.vertx.ext.web.handler.impl.HttpStatusException
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.reactivex.RxHelper

val bodyHandler: BodyHandler = BodyHandler.create()

object Routes {

    private val log = LoggerFactory.getLogger(Routes::class.java)

    fun setupRoutes(config: Config,
                    vertx: Vertx, router: Router,
                    userController: UserController,
                    transactionController: TransactionController,
                    reportController: ReportController,
                    currencyController: CurrencyController,
                    healthCheckRegistry: HealthCheckRegistry) {

        val jwtProvider = JWTAuth.create(vertx, JWTAuthOptions(keyStore = KeyStoreOptions(path = config.properties.jwt.keyStorePath, password = config.properties.jwt.password(config.profile))))
        val jwtAuthHandler = JWTAuthHandlerCustom(jwtProvider)
        val jwtRefreshAuthHandler = JWTRefreshAuthHandlerCustom(jwtProvider)
        val vertxScheduler = RxHelper.scheduler(vertx)

        router.route().handler(LoggerHandlerImpl(LoggerFormat.SHORT))
        handleUserRoutes(router, jwtAuthHandler, vertxScheduler, userController)
        handleTokenRoutes(config, router, jwtProvider, jwtRefreshAuthHandler, vertxScheduler, userController)
        handleTransactionRoutes(router, jwtAuthHandler, vertxScheduler, transactionController)
        handleReportRoutes(router, jwtAuthHandler, vertxScheduler, reportController)
        handleCurrenciesRoutes(router, jwtAuthHandler, vertxScheduler, currencyController)
        handleHealthRoutes(router, vertxScheduler, healthCheckRegistry)
        handleInfoRoutes(router)

        router.get("/")
            .handler { routingContext -> routingContext.response().end() }
            .failureHandler(Failure.failureHandler)

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

    fun HttpServerResponse.addContentTypeJson(): HttpServerResponse {
        this.putHeader("content-type", "application/json")
        return this
    }

    private class JWTAuthHandlerCustom(authProvider: JWTAuth) : JWTAuthHandlerImpl(authProvider, null) {

        override fun authorize(user: User, handler: Handler<AsyncResult<Void>>) {
            val isRefresh = user.principal().getBoolean("isRefresh")
            if (isRefresh) {
                throw HttpStatusException(401)
            }
            super.authorize(user, handler)
        }
    }

    class JWTRefreshAuthHandlerCustom(authProvider: JWTAuth) : JWTAuthHandlerImpl(authProvider, null) {

        override fun authorize(user: User, handler: Handler<AsyncResult<Void>>) {
            val isRefresh = user.principal().getBoolean("isRefresh")
            if (isRefresh) {
                super.authorize(user, handler)
                return
            }
            throw HttpStatusException(401)
        }
    }
}
