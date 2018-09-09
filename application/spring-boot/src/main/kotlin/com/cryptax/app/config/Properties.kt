package com.cryptax.app.config

import com.cryptax.config.AppProps
import com.cryptax.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Properties {

    @Value("\${spring.profiles.active}")
    lateinit var profile: String

    @Bean
    fun config(): Config {
        return Config(profile)
    }

    @Bean
    fun appProps(): AppProps {
        return config().properties
    }
}
