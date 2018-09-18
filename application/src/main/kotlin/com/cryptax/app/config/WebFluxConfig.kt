package com.cryptax.app.config

import com.cryptax.config.AppProps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class WebFluxConfig {

    @Autowired
    lateinit var appProps: AppProps

    @Bean
    fun corsFilter(): WebFilter {
        return WebFilter { ctx: ServerWebExchange, chain: WebFilterChain ->
            val request = ctx.request
            if (CorsUtils.isCorsRequest(request)) {
                val response = ctx.response
                val headers = response.headers
                if (appProps.server.allowOrigin == "*") {
                    headers.add("Access-Control-Allow-Origin", "*")
                } else if (request.uri.host.contains(appProps.server.allowOrigin)) {
                    headers.add("Access-Control-Allow-Origin", request.uri.host)
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
