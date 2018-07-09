package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import com.cryptax.validation.RestValidation
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler

object Routes {

	private val log = LoggerFactory.getLogger(Routes::class.java)

	private val userController = Config.userController
	private val transactionController = Config.transactionController
	private val jwtOptions = Config.jwtOptions

	fun setupRoutes(vertx: Vertx, router: Router) {
		val jwtProvider = JWTAuth.create(vertx, jwtOptions)
		val jwtAuthHandler = JWTAuthHandler.create(jwtProvider)
		val bodyHandler = BodyHandler.create()

		// Create user
		router.post("/users")
			.handler(bodyHandler)
			.handler(RestValidation.createUserValidation)
			.handler { event -> userController.createUser(event) }
			.failureHandler(failureHandler)

		// Get token with user credentials
		router.get("/token")
			.handler(RestValidation.loginValidation)
			.handler { event -> userController.login(event, jwtProvider) }
			.failureHandler(failureHandler)

		// Get user with user id and JWT token
		router.get("/users/:userId")
			.handler(jwtAuthHandler)
			.handler(RestValidation.getUserValidation)
			.handler { event -> userController.findUser(event) }
			.failureHandler(failureHandler)

		// Get all users with JWT token
		router.get("/users")
			.handler(jwtAuthHandler)
			.handler { event -> userController.findAllUser(event) }
			.failureHandler(failureHandler)

		// Add transaction to user
		router.post("/users/:userId/transactions")
			.handler(jwtAuthHandler)
			.handler(bodyHandler)
			.handler(RestValidation.addTransactionValidation)
			.handler { event -> transactionController.addTransaction(event) }
			.failureHandler(failureHandler)

		// Exception handler
		router.exceptionHandler { throwable ->
			log.error("Exception while processing a request", throwable)
		}
	}

	private val failureHandler: Handler<RoutingContext> = Handler { event ->
		val response = event.response()
		response.putHeader("content-type", "application/json")

		if (event.statusCode() == 401) {
			val userId: String? = event.user()?.principal()?.getString("userId")
			log.warn("Unauthorized request for user [$userId] and route [${event.currentRoute().path}]")
			response
				.setStatusCode(401)
				.end(JsonObject().put("error", "Unauthorized").encodePrettily())
		} else if (event.failure() != null) {
			val throwable: Throwable = event.failure()
			if (throwable is ValidationException) {
				log.debug("Validation exception [${throwable.message}]")
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
