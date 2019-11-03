package com.cryptax.app.micronaut.config

import com.cryptax.config.AppProps
import com.cryptax.config.Config
import com.cryptax.config.JwtProps
import com.cryptax.jwt.JwtService
import com.cryptax.jwt.TokenService
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.http.scope.RequestScope
import javax.inject.Inject
import javax.inject.Singleton

@Factory
internal class Properties {

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

    @Singleton
    @Bean
    fun tokenService(jwtProps: JwtProps): TokenService {
        return JwtService(jwtProps, context.environment.activeNames.iterator().next())
    }
}
