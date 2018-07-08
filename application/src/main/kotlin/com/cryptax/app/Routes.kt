package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.validation.RestValidation
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler

object Routes {

	private val userController = Config.userController
	private val transactionController = Config.transactionController
	private val jwtOptions = Config.jwtOptions

	fun setupRoutes(vertx: Vertx, router: Router) {
		val jwtProvider = JWTAuth.create(vertx, jwtOptions)
		val jwtAuthHandler = JWTAuthHandler.create(jwtProvider)

		router.route().handler(BodyHandler.create())

		// Create user
		router.post("/users")
			.handler(RestValidation.createUserValidation)
			.handler { event -> userController.createUser(event) }

		// Get token with user credentials
		router.get("/token")
			.handler(RestValidation.loginValidation)
			.handler { event -> userController.login(event, jwtProvider) }

		// Get user with user id and JWT token
		router.get("/users/:userId")
			.handler(jwtAuthHandler)
			.handler(RestValidation.getUserValidation)
			.handler { event -> userController.findUser(event) }

		// Get all users with JWT token
		router.get("/users")
			.handler(jwtAuthHandler)
			.handler { event -> userController.findAllUser(event) }

		// Add transaction to user
		router.post("/users/:userId/transactions")
			.handler(RestValidation.addTransactionValidation)
			.handler(jwtAuthHandler)
			.handler { event -> transactionController.addTransaction(event) }
	}
}
