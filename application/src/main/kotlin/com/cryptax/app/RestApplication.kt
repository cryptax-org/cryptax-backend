package com.cryptax.app

import com.cryptax.config.Config
import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
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

		// Create server
		vertx.createHttpServer(options)
			.requestHandler { router.accept(it) }.listen(8080)
	}

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			Launcher.executeCommand("run", RestApplication::class.java.name)
		}
	}
}
