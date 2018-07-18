package com.cryptax.app

import com.cryptax.app.routes.Routes
import com.cryptax.config.AppConfig
import com.cryptax.config.DefaultAppConfig
import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class RestApplication(private val appConfig: AppConfig = DefaultAppConfig()) : AbstractVerticle() {

    override fun start() {
        Json.mapper = AppConfig.objectMapper

        // Create router
        val router = Router.router(vertx)
        Routes.setupRoutes(appConfig, vertx, router)

        // Server options
        val options = HttpServerOptions()
        options.logActivity = true

        val port = appConfig.properties.server.port
        // Create server
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(port, appConfig.properties.server.domain) {
            if (it.failed()) {
                log.error("Fail to start the server", it.cause())
            } else {
                log.info("Server started on port $port with profile ${appConfig.getProfile()}")
            }
        }
    }

    companion object {

        init {
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        }

        private val log: Logger = LoggerFactory.getLogger(RestApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            Launcher.executeCommand("run", RestApplication::class.java.name)
        }
    }
}
