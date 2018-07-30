package com.cryptax.app.config

import com.cryptax.cache.CacheService
import com.cryptax.config.AppConfig
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.id.JugIdGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.ZonedDateTime

private val kodein = Kodein {
    import(TestAppConfig().appConfigKodein)
}

val objectMapper by kodein.instance<ObjectMapper>()

private fun testKodein(): Kodein.Module {
    return Kodein.Module(name = "testModule") {
        bind<UserRepository>() with singleton { InMemoryUserRepository() }
        bind<TransactionRepository>() with singleton { InMemoryTransactionRepository() }
        bind<IdGenerator>() with singleton { JugIdGenerator() }
        bind<EmailService>() with singleton { EmailServiceStub() }
        bind<CacheService>() with singleton { CacheServiceStub() }
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
