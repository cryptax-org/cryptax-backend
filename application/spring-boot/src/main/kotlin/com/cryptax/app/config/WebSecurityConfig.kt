package com.cryptax.app.config

import com.cryptax.app.jwt.SecurityContextRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {

    @Autowired
    lateinit var securityContextRepository: SecurityContextRepository

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.POST, "/users").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/email/*").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/email/*/reset").permitAll()
            .pathMatchers(HttpMethod.PUT, "/users/password").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/*/allow").permitAll()
            .pathMatchers(HttpMethod.POST, "/token").permitAll()
            .pathMatchers(HttpMethod.GET, "/refresh").permitAll()
            .anyExchange().authenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.I_AM_A_TEAPOT))
            .and()
            .build()
    }
}


