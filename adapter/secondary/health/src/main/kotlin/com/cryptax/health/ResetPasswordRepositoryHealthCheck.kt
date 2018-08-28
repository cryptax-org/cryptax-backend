package com.cryptax.health

import com.codahale.metrics.health.HealthCheck
import com.cryptax.domain.port.ResetPasswordRepository

class ResetPasswordRepositoryHealthCheck(private val resetPasswordRepository: ResetPasswordRepository) : HealthCheck() {

    override fun check(): Result {
        return if (resetPasswordRepository.ping()) {
            Result.healthy()
        } else {
            Result.unhealthy("Can't ping transaction repository")
        }
    }
}
