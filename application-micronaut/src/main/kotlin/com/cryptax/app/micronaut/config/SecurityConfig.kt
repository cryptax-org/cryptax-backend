package com.cryptax.app.micronaut.config

import com.cryptax.config.JwtProps
import com.cryptax.jwt.JwtService
import com.cryptax.jwt.TokenService
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import javax.inject.Inject
import javax.inject.Singleton

/*
@Factory
class SecurityConfig {

    // @Inject
    // lateinit var context: ApplicationContext

    @Inject
    lateinit var jwtProps: JwtProps

    @Singleton
    @Bean
    fun tokenService(): TokenService {
        return JwtService(jwtProps, "local")
        //return JwtService(jwtProps, context.environment.activeNames.iterator().next())
    }
}

*/
