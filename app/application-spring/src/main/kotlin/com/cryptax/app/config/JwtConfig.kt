package com.cryptax.app.config

import com.cryptax.config.AppProps
import com.cryptax.jwt.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Autowired
    lateinit var properties: AppProps

    @Value("\${spring.profiles.active}")
    lateinit var profile: String

    @Bean
    fun jwtService(): JwtService {
        return JwtService(jwtProps = properties.jwt, profile = profile)
    }
}
