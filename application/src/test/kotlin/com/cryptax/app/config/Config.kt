package com.cryptax.app.config

import com.cryptax.app.stub.CacheServiceStub
import com.cryptax.app.stub.EmailServiceStub
import com.cryptax.app.stub.PriceServiceStub
import com.cryptax.cache.CacheService
import com.cryptax.config.Config
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.di.KodeinConfig
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

fun kodein(): Kodein {
    val appConfig = TestConfig()
    val kodeinDefaultModule = KodeinConfig(properties = appConfig.properties, externalKodeinModule = testKodein()).kodeinModule
    return Kodein {
        import(kodeinDefaultModule, true)
    }
}

val objectMapper by kodein().instance<ObjectMapper>()

private fun testKodein(): Kodein.Module {
    return Kodein.Module(name = "testModule") {
        bind<UserRepository>(overrides = true) with singleton { InMemoryUserRepository() }
        bind<TransactionRepository>(overrides = true) with singleton { InMemoryTransactionRepository() }
        bind<IdGenerator>(overrides = true) with singleton { JugIdGenerator() }
        bind<EmailService>(overrides = true) with singleton { EmailServiceStub() }
        bind<CacheService>(overrides = true) with singleton { CacheServiceStub() }
        bind<PriceService>(overrides = true) with singleton { PriceServiceStub() }
    }
}

class TestConfig : Config(overrideProfile = "it")
