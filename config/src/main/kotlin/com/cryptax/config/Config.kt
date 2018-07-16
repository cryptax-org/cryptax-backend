package com.cryptax.config

import com.cryptax.config.jackson.JacksonConfig
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.id.JugIdGenerator
import com.cryptax.security.SecurePassword
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions


abstract class Config(userRepository: UserRepository, transactionRepository: TransactionRepository, idGenerator: IdGenerator) {

    private val securePassword = SecurePassword()
    private val createUser = CreateUser(userRepository, securePassword, idGenerator)
    private val findUser = FindUser(userRepository)
    private val loginUser = LoginUser(userRepository, securePassword)
    private val addTransaction = AddTransaction(transactionRepository, userRepository, idGenerator)
    private val updateTransaction = UpdateTransaction(transactionRepository)
    private val findTransaction = FindTransaction(transactionRepository)

    val userController = UserController(createUser, findUser, loginUser)
    val transactionController = TransactionController(addTransaction, updateTransaction, findTransaction)

    companion object {
        val objectMapper: ObjectMapper = JacksonConfig.objectMapper

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

class DefaultConfig(
    userRepository: UserRepository = InMemoryUserRepository(),
    transactionRepository: TransactionRepository = InMemoryTransactionRepository(),
    idGenerator: IdGenerator = JugIdGenerator()) : Config(userRepository, transactionRepository, idGenerator)

data class ConfigDto(val server: ServerDto, val jwt: JwtDto)
data class ServerDto(val domain: String, val port: Int)
data class JwtDto(val keyStorePath: String, val password: String, val algorithm: String, val issuer: String, val expiresInMinutes: Int, val refreshExpiresInDays: Int)
