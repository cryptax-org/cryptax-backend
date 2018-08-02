package com.cryptax.health

import com.codahale.metrics.health.HealthCheck
import com.cryptax.domain.port.TransactionRepository

class TransactionRepositoryHealthCheck(private val transactionRepository: TransactionRepository) : HealthCheck() {

    override fun check(): Result {
        return if (transactionRepository.ping()) {
            Result.healthy()
        } else {
            Result.unhealthy("Can't ping transaction repository")
        }
    }
}
