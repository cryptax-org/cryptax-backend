package com.cryptax.app.config

import com.cryptax.config.AppProps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxConfig : WebFluxConfigurer {

    @Autowired
    lateinit var appProps: AppProps

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(appProps.server.allowOrigin)
            .allowedMethods("GET, POST", "PUT", "DELETE")
            .allowedHeaders("Accept", "Content-Type", "Authorization")
    }
}
