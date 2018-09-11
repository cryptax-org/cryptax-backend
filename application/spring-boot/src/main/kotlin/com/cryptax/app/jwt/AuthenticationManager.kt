package com.cryptax.app.jwt

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Component
class AuthenticationManager : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val auth = UsernamePasswordAuthenticationToken(
            "derp",
            null,
            listOf(Role.ADMIN))
        return Mono.just(auth)
    }
}
