package com.cryptax.app.config

import com.cryptax.app.jwt.SecurityContextRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler

@Configuration
class WebSecurityConfig {

    @Autowired
    lateinit var securityContextRepository: SecurityContextRepository

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.GET, "/").permitAll()
            .pathMatchers(HttpMethod.POST, "/users").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/email/*").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/email/*/reset").permitAll()
            .pathMatchers(HttpMethod.PUT, "/users/password").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/*/allow").permitAll()
            .pathMatchers(HttpMethod.POST, "/token").permitAll()
            .pathMatchers(HttpMethod.GET, "/refresh").permitAll()
            .pathMatchers(HttpMethod.GET, "/info").permitAll()
            .pathMatchers(HttpMethod.GET, "/health").permitAll()
            .pathMatchers(HttpMethod.GET, "/ping").permitAll()
            .anyExchange().authenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.I_AM_A_TEAPOT))
            .and()
            .build()
    }
}

