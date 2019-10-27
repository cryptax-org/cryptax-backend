package com.cryptax.app.config

import com.cryptax.config.AppProps
import com.cryptax.config.Config
import com.cryptax.config.JwtProps
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import javax.inject.Inject
import javax.inject.Singleton

@Factory
class Properties {

    @Inject
    lateinit var context: ApplicationContext

    @Singleton
    fun config(): Config {
        return Config(context.environment.activeNames.iterator().next())
    }

    @Singleton
    fun appProps(): AppProps {
        return config().properties
    }

    @Singleton
    fun jtwProperties(): JwtProps {
        return appProps().jwt
    }
}
