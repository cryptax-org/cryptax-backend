package com.cryptax.config

import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.UserRepository
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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions
import java.time.ZoneId
import java.util.TimeZone

abstract class Config {

    abstract fun userController(): UserController

    abstract fun transactionController(): TransactionController

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val config: ConfigDto = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .readValue(Config::class.java.classLoader.getResourceAsStream("config-" + getProfile() + ".yml"), ConfigDto::class.java)

        val jwtAuthOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = config.jwt.keyStorePath, password = config.jwt.password))
        val jwtOptions = JWTOptions(
            algorithm = config.jwt.algorithm,
            issuer = config.jwt.issuer,
            expiresInMinutes = config.jwt.expiresInMinutes)
        val jwtRefreshOptions = JWTOptions(
            algorithm = config.jwt.algorithm,
            issuer = config.jwt.issuer,
            expiresInMinutes = config.jwt.refreshExpiresInDays)

        fun getProfile(): String {
            // TODO find a better way to handle profiles
            val profile = System.getenv("PROFILE")
            return profile ?: return "dev"
        }
    }
}

class DefaultConfig : Config() {

    private val userRepository: UserRepository = InMemoryUserRepository()
    private val transactionRepository = InMemoryTransactionRepository()
    private val idGenerator = JugIdGenerator()
    private val securePassword = SecurePassword()
    private val createUser = CreateUser(userRepository, securePassword, idGenerator)
    private val findUser = FindUser(userRepository)
    private val loginUser = LoginUser(userRepository, securePassword)
    private val addTransaction = AddTransaction(transactionRepository, userRepository, idGenerator)
    private val updateTransaction = UpdateTransaction(transactionRepository)
    private val findTransaction = FindTransaction(transactionRepository)

    override fun userController(): UserController {
        return userControllerInternal
    }

    override fun transactionController(): TransactionController {
        return transactionControllerInternal
    }

    private val userControllerInternal: UserController by lazy {
        UserController(createUser, findUser, loginUser)
    }

    private val transactionControllerInternal: TransactionController by lazy {
        TransactionController(addTransaction, updateTransaction, findTransaction)
    }
}

data class ConfigDto(val server: ServerDto, val jwt: JwtDto)
data class ServerDto(val domain: String, val port: Int)
data class JwtDto(val keyStorePath: String, val password: String, val algorithm: String, val issuer: String, val expiresInMinutes: Int, val refreshExpiresInDays: Int)
