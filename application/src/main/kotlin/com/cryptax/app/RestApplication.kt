package com.cryptax.app

import com.cryptax.config.Config
import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class RestApplication : AbstractVerticle() {

	override fun start() {
		Json.mapper = Config.objectMapper

		// Create router
		val router = Router.router(vertx)
		Routes.setupRoutes(vertx, router)

		// Server options
		val options = HttpServerOptions()
		options.logActivity = true

		val port = Config.config.server.port
		// Create server
		vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(port, Config.config.server.domain) {
			if (it.failed()) {
				log.error("Fail to start the server")
			} else {
				log.info("Server started on port $port")
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
