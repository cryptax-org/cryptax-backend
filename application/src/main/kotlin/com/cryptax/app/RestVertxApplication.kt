package com.cryptax.app

import com.cryptax.config.VertxConfig
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.core.json.Json
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions

class RestVertxApplication : AbstractVerticle() {

	private val userController = VertxConfig.vertxUserController
	// FIXME: Secure keystore and password
	private val config = JWTAuthOptions(keyStore = KeyStoreOptions(path = "keystore.jceks", password = "secret"))

	override fun start() {
		Json.mapper = ObjectMapper().registerModule(KotlinModule())
		Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

		val provider = JWTAuth.create(vertx, config)
		val jwtAuthHandler = JWTAuthHandler.create(provider)

		val router = Router.router(vertx)
		router.route().handler(BodyHandler.create())

		router.post("/users")
			.handler { event -> userController.createUser(event) }

		router.get("/login")
			.handler { event -> userController.login(event, provider) }

		router.get("/users/:userId")
			.handler(jwtAuthHandler)
			.handler { event -> userController.findUser(event) }

		router.get("/users")
			.handler(jwtAuthHandler)
			.handler { event -> userController.findAllUser(event) }

		vertx.createHttpServer().requestHandler { router.accept(it) }.listen(8080)
	}

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			Launcher.executeCommand("run", RestVertxApplication::class.java.name)
		}
	}
}
