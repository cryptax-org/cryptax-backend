package com.cryptax.app.config

import com.cryptax.app.jwt.SecurityContextRepository
import com.cryptax.config.AppProps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class WebConfig {

    @Autowired
    lateinit var securityContextRepository: SecurityContextRepository

    @Autowired
    lateinit var appProps: AppProps

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            // TODO: use .cors() instead of the corsfilter
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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

    @Bean
    fun corsFilter(): WebFilter {
        return WebFilter { ctx: ServerWebExchange, chain: WebFilterChain ->
            val request = ctx.request
            if (CorsUtils.isCorsRequest(request)) {
                val response = ctx.response
                val headers = response.headers
                if (appProps.server.allowOrigin == "*") {
                    headers.add("Access-Control-Allow-Origin", "*")
                } else {
                    request.headers[HttpHeaders.ORIGIN]!!
                        .find { origin -> origin.contains(appProps.server.allowOrigin) }
                        .apply { headers.add("Access-Control-Allow-Origin", this!!) }
                }
                headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS)
                headers.add("Access-Control-Max-Age", MAX_AGE)
                headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS)
                if (request.method === HttpMethod.OPTIONS) {
                    response.statusCode = HttpStatus.OK
                    Mono.empty()
                } else {
                    chain.filter(ctx)
                }
            } else {
                chain.filter(ctx)
            }
        }
    }

    companion object {
        private const val ALLOWED_HEADERS = "Accept, Content-Type, Authorization"
        private const val ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS"
        private const val MAX_AGE = "3600"
    }
}
