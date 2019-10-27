package com.cryptax.app.jwt

/*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository : ServerSecurityContextRepository {

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    override fun load(serverWebExchange: ServerWebExchange): Mono<SecurityContext> {
        val token = jwtTokenProvider.resolveToken(serverWebExchange.request)

        if (token.isNotBlank()) {
            if (jwtTokenProvider.validateToken(token)) {
                val authentication = jwtTokenProvider.getUserAuthentication(token)
                return Mono.just(SecurityContextImpl(authentication))
            }
        }
        return Mono.empty<SecurityContext>()
    }

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        return Mono.error(RuntimeException("not implemented"))
    }
}
*/
