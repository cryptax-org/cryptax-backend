package com.cryptax.app

import com.cryptax.config.VertxConfig
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RestVertxApplication : AbstractVerticle() {

	private val userController = VertxConfig.vertxUserController

	override fun start() {
		Json.mapper = ObjectMapper().registerModule(KotlinModule())
		Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
		val router = Router.router(vertx)
		router.route().handler(BodyHandler.create())
		router.post("/users").handler { event -> userController.createUser(event) }
		router.get("/login").handler { event -> userController.login(event) }
		router.get("/users/:userId").handler { event -> userController.findUser(event) }
		router.get("/users").handler { event -> userController.findAllUser(event) }
		vertx.createHttpServer().requestHandler { router.accept(it) }.listen(8080)
	}

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			Launcher.executeCommand("run", RestVertxApplication::class.java.name)
		}
	}
}
