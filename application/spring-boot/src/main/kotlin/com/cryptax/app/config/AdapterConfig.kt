package com.cryptax.app.config

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.config.AppProps
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.email.SendGridEmailService
import com.cryptax.health.ResetPasswordRepositoryHealthCheck
import com.cryptax.health.TransactionRepositoryHealthCheck
import com.cryptax.health.UserRepositoryHealthCheck
import com.cryptax.id.JugIdGenerator
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class AdapterConfig {

    @Autowired
    lateinit var properties: AppProps

    @Bean
    fun httpClient(): OkHttpClient {
        val connectionPool = ConnectionPool(properties.http.maxIdleConnections, properties.http.keepAliveDuration, TimeUnit.MINUTES)
        val builder = OkHttpClient.Builder().connectionPool(connectionPool)
        return builder.build()
    }

    @Bean
    fun emailService(): EmailService {
        return SendGridEmailService(httpClient(), properties.email)
    }

    @Bean
    fun securePassword(): SecurePassword {
        return com.cryptax.security.SecurePassword()
    }

    @Bean
    fun idGenerator(): IdGenerator {
        return JugIdGenerator()
    }

    @Bean
    fun userRepositoryHealthCheck(userRepository: UserRepository): UserRepositoryHealthCheck {
        return UserRepositoryHealthCheck(userRepository)
    }

    @Bean
    fun transactionRepositoryHealthCheck(transactionRepository: TransactionRepository): TransactionRepositoryHealthCheck {
        return TransactionRepositoryHealthCheck(transactionRepository)
    }

    @Bean
    fun resetPasswordRepository(resetPasswordRepository: ResetPasswordRepository): ResetPasswordRepositoryHealthCheck {
        return ResetPasswordRepositoryHealthCheck(resetPasswordRepository)
    }

    @Bean
    fun healthCheckRegistry(
        userRepositoryHealthCheck: UserRepositoryHealthCheck,
        transactionRepositoryHealthCheck: TransactionRepositoryHealthCheck,
        resetPasswordRepositoryCheck: ResetPasswordRepositoryHealthCheck): HealthCheckRegistry {
        val healthCheckRegistry = HealthCheckRegistry()
        healthCheckRegistry.register("userRepository", userRepositoryHealthCheck)
        healthCheckRegistry.register("transactionRepository", transactionRepositoryHealthCheck)
        healthCheckRegistry.register("resetPasswordRepository", resetPasswordRepositoryCheck)
        return healthCheckRegistry
    }
}
