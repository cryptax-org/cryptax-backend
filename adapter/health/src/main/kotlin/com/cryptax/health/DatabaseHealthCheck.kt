package com.cryptax.health

import com.codahale.metrics.health.HealthCheck
import com.cryptax.domain.port.UserRepository

class DatabaseHealthCheck(private val userRepository: UserRepository) : HealthCheck() {

    override fun check(): Result {
        return if (userRepository.ping()) {
            Result.healthy()
        } else {
            Result.unhealthy("Can't ping database")
        }
    }
}
