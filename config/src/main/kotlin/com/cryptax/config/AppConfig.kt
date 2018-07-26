package com.cryptax.config

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.config.dto.PropertiesDto
import com.cryptax.config.jackson.JacksonConfig
import com.cryptax.controller.ReportController
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.health.TransactionRepositoryHealthCheck
import com.cryptax.health.UserRepositoryHealthCheck
import com.cryptax.id.JugIdGenerator
import com.cryptax.price.PriceService
import com.cryptax.security.SecurePassword
import com.cryptax.usecase.report.GenerateReport
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
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

abstract class AppConfig(private val profile: String = "dev", kodeinModule: Kodein.Module) {

    val appConfigKodein = Kodein.Module(name = "defaultModule") {
        // Import module binding
        import(kodeinModule)

        // Usecases
        bind() from singleton { CreateUser(instance(), instance(), instance(), instance()) }
        bind() from singleton { FindUser(instance()) }
        bind() from singleton { ValidateUser(instance(), instance()) }
        bind() from singleton { LoginUser(instance(), instance()) }
        bind() from singleton { AddTransaction(instance(), instance(), instance()) }
        bind() from singleton { UpdateTransaction(instance()) }
        bind() from singleton { FindTransaction(instance()) }
        bind() from singleton { GenerateReport(instance(), instance(), instance()) }

        // Controllers
        bind() from singleton { UserController(instance(), instance(), instance(), instance()) }
        bind() from singleton { TransactionController(instance(), instance(), instance()) }
        bind() from singleton { ReportController(instance()) }

        // Health
        bind() from singleton {
            val healthCheckRegistry = HealthCheckRegistry()
            healthCheckRegistry.register("userRepository", instance("userRepositoryCheck"))
            healthCheckRegistry.register("transactionRepository", instance("transactionRepositoryCheck"))
            healthCheckRegistry
        }
        bind<HealthCheck>("userRepositoryCheck") with singleton { UserRepositoryHealthCheck(instance()) }
        bind<HealthCheck>("transactionRepositoryCheck") with singleton { TransactionRepositoryHealthCheck(instance()) }

        // Other
        bind<com.cryptax.domain.port.SecurePassword>() with singleton { SecurePassword() }
        bind<com.cryptax.domain.port.PriceService>() with singleton { PriceService(instance(), instance()) }
        bind<ObjectMapper>() with singleton { JacksonConfig.objectMapper }
        bind<OkHttpClient>() with singleton { OkHttpClient() } // TODO handle thread pool
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
}

private val defaultKodein = Kodein.Module(name = "defaultUserModule") {
    bind<UserRepository>() with singleton { InMemoryUserRepository() }
    bind<TransactionRepository>() with singleton { InMemoryTransactionRepository() }
    bind<IdGenerator>() with singleton { JugIdGenerator() }
    bind<EmailService>() with singleton {
        object : EmailService {
            override fun welcomeEmail(user: User, token: String) {}
        }
    }
}

class DefaultAppConfig(kodeinModule: Kodein.Module = defaultKodein) : AppConfig(kodeinModule = kodeinModule)
