package com.cryptax.app.stub

import com.cryptax.cache.CacheService
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.PriceService
import java.time.ZoneId
import java.time.ZonedDateTime

class EmailServiceStub : EmailService {
    override fun welcomeEmail(user: User, token: String) {}
    override fun resetPasswordEmail(email: String, resetPassword: ResetPassword) {}
    override fun resetPasswordConfirmationEmail(email: String) {}
}

class CacheServiceStub : CacheService {
    override fun get(name: String, currency: Currency, date: ZonedDateTime): Pair<String, Double>? {
        return null
    }

    override fun put(name: String, currency: Currency, date: ZonedDateTime, value: Pair<String, Double>) {
    }
}

class PriceServiceStub : PriceService {
    companion object {
        private const val serviceName = "Stub"
    }

    override fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Pair<String, Double> {
        return when (currency) {
            Currency.BTC -> {
                when (date) {
                    ZonedDateTime.of(2011, 12, 3, 10, 15, 30, 0, ZoneId.of("UTC")) -> Pair(serviceName, 100.0)
                    else -> throw RuntimeException("Stubbing issue, date [$date] not handled")
                }
            }
            Currency.ETH -> {
                when (date) {
                    ZonedDateTime.of(2011, 12, 3, 10, 15, 30, 0, ZoneId.of("UTC")) -> Pair(serviceName, 0.0)
                    else -> throw RuntimeException("Stubbing issue, date [$date] not handled")
                }
            }
            else -> throw RuntimeException("Stubbing issue, currency not handled")
        }
    }
}
