package com.cryptax.config

import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.id.JugIdGenerator
import com.cryptax.security.SecurePassword
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions
import java.time.ZoneId
import java.util.TimeZone

object Config {

	private val userRepository = InMemoryUserRepository()
	private val transactionRepository = InMemoryTransactionRepository()

	private val idGenerator = JugIdGenerator()
	private val securePassword = SecurePassword()
	private val createUser = CreateUser(userRepository, securePassword, idGenerator)
	private val findUser = FindUser(userRepository)
	private val loginUser = LoginUser(userRepository, securePassword)
	private val addTransaction = AddTransaction(transactionRepository, userRepository, idGenerator)
	private val updateTransaction = UpdateTransaction(transactionRepository)
	private val findTransaction = FindTransaction(transactionRepository)

	val userController = UserController(createUser, findUser, loginUser)
	val transactionController = TransactionController(addTransaction, updateTransaction, findTransaction)

	val objectMapper: ObjectMapper = ObjectMapper()
		.registerModule(KotlinModule())
		.registerModule(JavaTimeModule())
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
		.setSerializationInclusion(JsonInclude.Include.NON_NULL)

	// FIXME: Secure keystore and password
	val jwtAuthOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = "keystore.jceks", password = "secret"))
	val jwtOptions = JWTOptions(algorithm = "ES512", issuer = "Cryptax", expiresInMinutes = 30)

}
