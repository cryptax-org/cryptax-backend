package com.cryptax.app.jwt

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.SecurityContextServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


class JwtTokenFilter(private val jwtTokenProvider: JwtTokenProvider) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val token = jwtTokenProvider.resolveToken(request)
        if (token.isNotEmpty() && jwtTokenProvider.validateToken(token)) {
            val auth = jwtTokenProvider.getAuthentication(token)
            //SecurityContextHolder.getContext().authentication = auth
            //ReactiveSecurityContextHolder.withAuthentication(auth)
            //WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
            ReactiveSecurityContextHolder.clearContext()
            val securityContext = SecurityContextImpl()
            securityContext.authentication = auth
/*            return this.securityContextRepository.save(exchange, securityContext)
                .then(this.authenticationSuccessHandler
                    .onAuthenticationSuccess(webFilterExchange, authentication))
                .subscriberContext(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))\*/
            val context = ReactiveSecurityContextHolder.withAuthentication(auth)
            return chain.filter(SecurityContextServerWebExchange(exchange, Mono.just(securityContext)))
        }
        return chain.filter(exchange)
    }
}
