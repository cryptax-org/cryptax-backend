package com.cryptax.config

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.config.jackson.JacksonConfig
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.email.VertxEmailService
import com.cryptax.health.DatabaseHealthCheck
import com.cryptax.id.JugIdGenerator
import com.cryptax.security.SecurePassword
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ValidateUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

abstract class AppConfig(
    private val profile: String = "dev",
    userRepository: UserRepository,
    transactionRepository: TransactionRepository,
    idGenerator: IdGenerator,
    emailService: EmailService) {

    private val securePassword = SecurePassword()
    private val createUser = CreateUser(userRepository, securePassword, idGenerator, emailService)
    private val findUser = FindUser(userRepository)
    private val validateUser = ValidateUser(userRepository, securePassword)
    private val loginUser = LoginUser(userRepository, securePassword)
    private val addTransaction = AddTransaction(transactionRepository, userRepository, idGenerator)
    private val updateTransaction = UpdateTransaction(transactionRepository)
    private val findTransaction = FindTransaction(transactionRepository)

    val userController = UserController(createUser, findUser, loginUser, validateUser)
    val transactionController = TransactionController(addTransaction, updateTransaction, findTransaction)
    val register: HealthCheckRegistry = HealthCheckRegistry()

    init {
        register.register("database", DatabaseHealthCheck(userRepository))
    }

    val properties: PropertiesDto = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue(AppConfig::class.java.classLoader.getResourceAsStream("config-${getProfile()}.yml"), PropertiesDto::class.java)

    val jwtAuthOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = properties.jwt.keyStorePath, password = properties.jwt.password(profile)))
    val jwtOptions = JWTOptions(
        algorithm = properties.jwt.algorithm,
        issuer = properties.jwt.issuer,
        expiresInMinutes = properties.jwt.expiresInMinutes)
    val jwtRefreshOptions = JWTOptions(
        algorithm = properties.jwt.algorithm,
        issuer = properties.jwt.issuer,
        expiresInMinutes = properties.jwt.refreshExpiresInDays)

    fun getProfile(): String {
        val profileEnv = System.getenv("PROFILE")
        return profileEnv ?: return profile
    }

    companion object {
        val objectMapper = JacksonConfig.objectMapper
    }
}

class DefaultAppConfig(
    userRepository: UserRepository = InMemoryUserRepository(),
    transactionRepository: TransactionRepository = InMemoryTransactionRepository(),
    idGenerator: IdGenerator = JugIdGenerator(),
    emailService: EmailService = VertxEmailService())
    : AppConfig(userRepository = userRepository, transactionRepository = transactionRepository, idGenerator = idGenerator, emailService = emailService)

data class PropertiesDto(val server: ServerDto, val jwt: JwtDto)
data class ServerDto(val domain: String, val port: Int)
data class JwtDto(
    val keyStorePath: String,
    private var password: String,
    val algorithm: String,
    val issuer: String,
    val expiresInMinutes: Int,
    val refreshExpiresInDays: Int) {

    fun password(profile: String): String {
        if (profile == "it") return password
        return decryptPassword(password)
    }

    private fun decryptPassword(password: String): String {
        val stringEncryptor = StandardPBEStringEncryptor()
        val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
        val argument = runtimeMxBean.inputArguments.find { s -> s.contains("jasypt.encryptor.password") } ?: throw RuntimeException("jasypt.encryptor.password not found")
        val jasyptPassword = argument.substring(argument.indexOf("=") + 1)
        stringEncryptor.setPassword(jasyptPassword)
        return stringEncryptor.decrypt(password)
    }
}
