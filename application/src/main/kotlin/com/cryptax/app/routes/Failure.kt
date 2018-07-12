package com.cryptax.app.routes

import com.cryptax.app.routes.Routes.addContentTypeJson
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException

object Failure {

	private val log = LoggerFactory.getLogger(Failure::class.java)

	val failureHandler: Handler<RoutingContext> = Handler { event ->
		val response = event.response().addContentTypeJson()

		if (event.statusCode() == 401) {
			val userId: String? = event.user()?.principal()?.getString("userId")
			log.warn("Unauthorized request for user [$userId] and route [${event.currentRoute().path}]")
			response
				.setStatusCode(401)
				.end(JsonObject().put("error", "Unauthorized").encodePrettily())
		} else if (event.failure() != null) {
			val throwable: Throwable = event.failure()
			if (throwable is ValidationException) {
				log.warn("Validation exception [${throwable.message}]")
				response
					.setStatusCode(400)
					.end(JsonObject().put("error", "${throwable.message}").encodePrettily())
			} else if (throwable is LoginException) {
				log.warn("Unauthorized request for user [${throwable.email}] and with description [${throwable.description}]")
				response
					.setStatusCode(401)
					.end(JsonObject().put("error", "Unauthorized").encodePrettily())
			} else if (throwable is UserNotFoundException) {
				log.warn("User not found [${throwable.message}]")
				response
					.setStatusCode(400)
					.end(JsonObject().put("error", "Bad request").encodePrettily())
			} else if (throwable is UserAlreadyExistsException) {
				log.warn("User already exists [${throwable.message}]")
				response
					.setStatusCode(400)
					.end(JsonObject().put("error", "Bad request").encodePrettily())
			} else if (throwable is UserValidationException || throwable is TransactionValidationException) {
				log.debug("Validation exception [${throwable.message}]")
				response
					.setStatusCode(400)
					.end(JsonObject().put("error", "${throwable.message}").encodePrettily())
			} else {
				log.error("Exception type [${throwable.javaClass.simpleName}] not handled. Should the devs handle it?", throwable)
				response
					.setStatusCode(500)
					.end(JsonObject().put("error", "Something went wrong").encodePrettily())
			}
		} else {
			log.error("Something failed, but we were not able to know why")
			response
				.setStatusCode(500)
				.end(JsonObject().put("error", "Something went wrong").encodePrettily())
		}
	}
}
