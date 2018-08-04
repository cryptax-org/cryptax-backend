package com.cryptax.config.kodein

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.cache.CacheService
import com.cryptax.config.dto.PropertiesDto
import com.cryptax.config.jackson.JacksonConfig
import com.cryptax.controller.ReportController
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.Currency
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
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class KodeinConfig(properties: PropertiesDto, externalKodeinModule: Kodein.Module?) {

    val kodeinModule = Kodein.Module(name = "defaultModule") {

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
        bind<ObjectMapper>() with singleton { JacksonConfig.objectMapper }
        bind<OkHttpClient>() with singleton {
            val connectionPool = ConnectionPool(properties.http.maxIdleConnections, properties.http.keepAliveDuration, TimeUnit.MINUTES)
            val builder = OkHttpClient.Builder().connectionPool(connectionPool)
            builder.build()
        }

        bind<UserRepository>() with singleton { InMemoryUserRepository() }
        bind<TransactionRepository>() with singleton { InMemoryTransactionRepository() }
        bind<IdGenerator>() with singleton { JugIdGenerator() }
        bind<com.cryptax.domain.port.SecurePassword>() with singleton { SecurePassword() }

        bind<com.cryptax.domain.port.PriceService>() with singleton {
            PriceService(client = instance(), objectMapper = instance(), cache = instance())
        }

        // This will be overridden later
        bind<EmailService>() with singleton {
            object : EmailService {
                override fun welcomeEmail(user: User, token: String) {}
            }
        }
        bind<CacheService>() with singleton {
            object : CacheService {
                override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {}
                override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
                    return null
                }
            }
        }

        // Override binding if needed
        if (externalKodeinModule != null) {
            import(externalKodeinModule, allowOverride = true)
        }
    }
}
