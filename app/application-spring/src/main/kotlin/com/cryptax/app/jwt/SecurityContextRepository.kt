package com.cryptax.app.jwt

import com.cryptax.jwt.TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository : ServerSecurityContextRepository {

    @Autowired
    lateinit var tokenService: TokenService

    override fun load(serverWebExchange: ServerWebExchange): Mono<SecurityContext> {
        val token = extractToken(serverWebExchange.request)

        if (token.isNotBlank()) {
            if (tokenService.validateToken(token)) {
                val authentication = getUserAuthentication(token)
                return Mono.just(SecurityContextImpl(authentication))
            }
        }
        return Mono.empty<SecurityContext>()
    }

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        return Mono.error(RuntimeException("not implemented"))
    }

    private fun getUserAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val tokenDetails = tokenService.tokenDetails(token).blockingGet()
        return UsernamePasswordAuthenticationToken(tokenDetails.subject, token, tokenDetails.roles.map { SimpleGrantedAuthority(it.name) })
    }
}
