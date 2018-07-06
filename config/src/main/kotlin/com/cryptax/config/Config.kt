package com.cryptax.config

import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.encoder.Sha256PasswordEncoder
import com.cryptax.id.JugIdGenerator
import com.cryptax.usecase.CreateUser
import com.cryptax.usecase.FindUser
import com.cryptax.usecase.LoginUser
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions

object Config {

	private val userRepository = InMemoryUserRepository()
	private val idGenerator = JugIdGenerator()
	private val passwordEncoder = Sha256PasswordEncoder()
	private val createUser = CreateUser(userRepository, passwordEncoder, idGenerator)
	private val findUser = FindUser(userRepository)
	private val loginUser = LoginUser(userRepository, passwordEncoder)
	val userController = UserController(createUser, findUser, loginUser)

	val objectMapper: ObjectMapper = ObjectMapper()
		.registerModule(KotlinModule())
		.setSerializationInclusion(JsonInclude.Include.NON_NULL)

	// FIXME: Secure keystore and password
	val jwtOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = "keystore.jceks", password = "secret"))
}
