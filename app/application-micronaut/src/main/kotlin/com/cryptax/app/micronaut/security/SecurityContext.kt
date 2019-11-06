package com.cryptax.app.micronaut.security

import com.cryptax.app.micronaut.web.extractToken
import com.cryptax.jwt.TokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.http.scope.RequestScope
import io.reactivex.Single

@RequestScope
class SecurityContext(private val tokenService: TokenService) {

    private val currentToken: String = extractToken(ServerRequestContext.currentRequest<Any>().get())
    private val token: Single<String>

    init {
        token = Single.just(currentToken)
    }

    private fun loadSecurityContext(): Single<Authentication> {
        return token
            .filter { token -> token.isNotBlank() }
            .flatMap { token -> tokenService.validateTokenRx(token) }
            .flatMap { token -> getCurrentAuthentication(token).toMaybe() }
            .switchIfEmpty(Single.just(DefaultAuthentication()))
            .cache()
    }

    fun validateUserId(userId: String): Single<Authentication> {
        return loadSecurityContext()
            .map { authentication ->
                if (authentication.isAuthenticated() && userId == (authentication as UserAuthentication).principal) {
                    authentication
                } else {
                    throw SecurityContextException("User [$userId] can't be accessed with the given token [$currentToken]")
                }
            }
    }

    fun validateRequest(): Single<Authentication> {
        return loadSecurityContext()
            .map { authentication ->
                if (authentication.isAuthenticated()) {
                    authentication
                } else {
                    throw SecurityContextException("User is not authenticated. Token used: [$currentToken]")
                }
            }
    }

    private fun getCurrentAuthentication(token: String): Single<Authentication> {
        return tokenService.tokenDetails(token)
            .map { details -> UserAuthentication(details.subject, token, details.roles.map { it.name }) }
    }
}
