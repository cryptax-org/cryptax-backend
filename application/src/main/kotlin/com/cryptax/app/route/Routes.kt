package com.cryptax.app.route

import com.cryptax.app.jwt.JwtException
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

internal fun verifyUserId(userId: String): Mono<Boolean> {
    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { context ->
            when (context.authentication.principal as String) {
                userId -> Mono.just(true)
                else -> Mono.error(JwtException("User $userId can't be accessed with the given token ${context.authentication.credentials}"))
            }
        }
}
