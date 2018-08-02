package com.cryptax.app.config

import com.cryptax.cache.CacheService
import com.cryptax.config.AppConfig
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.id.JugIdGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.ZoneId
import java.time.ZonedDateTime

private val kodein = Kodein {
    import(TestAppConfig().kodeinDefaultModule, true)
}

val objectMapper by kodein.instance<ObjectMapper>()

private fun testKodein(): Kodein.Module {
    return Kodein.Module(name = "testModule") {
        bind<UserRepository>() with singleton { InMemoryUserRepository() }
        bind<TransactionRepository>() with singleton { InMemoryTransactionRepository() }
        bind<IdGenerator>() with singleton { JugIdGenerator() }
        bind<EmailService>() with singleton { EmailServiceStub() }
        bind<CacheService>() with singleton { CacheServiceStub() }
        bind<PriceService>() with singleton { PriceServiceStub() }
    }
}

class TestAppConfig : AppConfig("it", testKodein())

class EmailServiceStub : EmailService {
    override fun welcomeEmail(user: User, token: String) {}
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
