package com.cryptax.app.routes

import com.cryptax.config.Config
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.ext.web.handler.LoggerHandler

val bodyHandler: BodyHandler = BodyHandler.create()
/*val userController = config.userController
val jwtOptions = Config.jwtOptions*/

object Routes {

	private val log = LoggerFactory.getLogger(Routes::class.java)

	fun setupRoutes(config: Config, vertx: Vertx, router: Router) {
		val jwtProvider = JWTAuth.create(vertx, Config.jwtAuthOptions)
		val jwtAuthHandler = JWTAuthHandler.create(jwtProvider)

		router.route().handler(LoggerHandler.create())
		handleUserRoutes(config, router, jwtAuthHandler)
		handleTokenRoutes(config, router, jwtProvider)
		handleTransactionRoutes(config, router, jwtAuthHandler)

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
