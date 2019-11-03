package com.cryptax.app.micronaut.security

import com.cryptax.jwt.TokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.http.scope.RequestScope
import io.reactivex.Single

@RequestScope
class SecurityContext(private val tokenService: TokenService) {

    private val currentToken: String
    private val token: Single<String>

    init {
        currentToken = extractToken(ServerRequestContext.currentRequest<Any>().get())
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

    fun validateRequest(): Single<Boolean> {
        return loadSecurityContext()
            .map { authentication ->
                if (authentication.isAuthenticated()) {
                    authentication.isAuthenticated()
                } else {
                    throw SecurityContextException("User is not authenticated. Token used: [$currentToken]")
                }
            }
    }

    private fun extractToken(currentRequest: HttpRequest<Any>?): String {
        if (currentRequest == null) return ""
        val authHeader: String? = currentRequest.headers.authorization?.orElseGet { "" }
        if (authHeader != null && authHeader.isNotBlank() && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }
        return ""
    }

    private fun getCurrentAuthentication(token: String): Single<Authentication> {
        return tokenService.tokenDetails(token)
            .map { details -> UserAuthentication(details.subject, token, details.roles.map { it.name }) }
    }
}
