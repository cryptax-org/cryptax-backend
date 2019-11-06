package com.cryptax.app.micronaut.config

import com.codahale.metrics.health.HealthCheckRegistry
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.health.ResetPasswordRepositoryHealthCheck
import com.cryptax.health.TransactionRepositoryHealthCheck
import com.cryptax.health.UserRepositoryHealthCheck
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class HealthConfig {

    @Singleton
    fun userRepositoryHealthCheck(userRepository: UserRepository): UserRepositoryHealthCheck {
        return UserRepositoryHealthCheck(userRepository)
    }

    @Singleton
    fun transactionRepositoryHealthCheck(transactionRepository: TransactionRepository): TransactionRepositoryHealthCheck {
        return TransactionRepositoryHealthCheck(transactionRepository)
    }

    @Singleton
    fun resetPasswordRepository(resetPasswordRepository: ResetPasswordRepository): ResetPasswordRepositoryHealthCheck {
        return ResetPasswordRepositoryHealthCheck(resetPasswordRepository)
    }

    @Singleton
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
