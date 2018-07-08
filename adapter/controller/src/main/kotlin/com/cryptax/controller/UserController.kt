package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.controller.utils.sendError
import com.cryptax.controller.utils.sendSuccess
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.ext.auth.jwt.JWTOptions

class UserController(private val createUser: CreateUser, private val findUser: FindUser, private val loginUser: LoginUser) {

	companion object {
		private val JWT_OPTIONS = JWTOptions(algorithm = "ES512", issuer = "Cryptax", expiresInMinutes = 30)
	}

	fun createUser(routingContext: RoutingContext) {
		val response = routingContext.response()
		val body = routingContext.body
		val userWeb = body.toJsonObject().mapTo(UserWeb::class.java)
		val user = createUser.create(userWeb.toUser())
		val result = JsonObject.mapFrom(UserWeb.toUserWeb(user))
		sendSuccess(result, response)
	}

	fun login(routingContext: RoutingContext, jwtProvider: JWTAuth) {
		val email = routingContext.request().getParam("email")
		val password = routingContext.request().getParam("password").toCharArray()

		val userId = loginUser.login(email, password).id
		val result = JsonObject().put("id", userId)
		val token = jwtProvider.generateToken(result, JWT_OPTIONS)
		result.put("token", token)
		sendSuccess(result, routingContext.response())
	}

	fun findUser(routingContext: RoutingContext) {
		val response = routingContext.response()
		val userId = routingContext.request().getParam("userId")
		if (routingContext.user().principal().getString("id") == userId) {
			val user = findUser.findById(userId)
			if (user != null) {
				val result = JsonObject.mapFrom(UserWeb.toUserWeb(user))
				sendSuccess(result, response)
			} else {
				sendError(404, response)
			}
		} else {
			sendError(401, response)
		}
	}

	fun findAllUser(routingContext: RoutingContext) {
		val users = findUser.findAllUsers()
		val result: JsonArray = users
			.map { user -> JsonObject.mapFrom(UserWeb.toUserWeb(user)) }
			.fold(mutableListOf<JsonObject>()) { accumulator, item ->
				accumulator.add(item)
				accumulator
			}
			.fold(JsonArray()) { accumulator, item ->
				accumulator.add(item)
				accumulator
			}

		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(result.encodePrettily())
	}
}
