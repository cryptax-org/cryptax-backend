package com.cryptax.app.config

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.health.ResetPasswordRepositoryHealthCheck
import com.cryptax.health.TransactionRepositoryHealthCheck
import com.cryptax.health.UserRepositoryHealthCheck
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthConfig {

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
