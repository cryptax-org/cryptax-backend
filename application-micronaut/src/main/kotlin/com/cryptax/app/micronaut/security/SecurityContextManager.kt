package com.cryptax.app.micronaut.security

import com.cryptax.jwt.TokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.http.scope.RequestScope
import io.reactivex.Single

@RequestScope
class SecurityContextManager(private val tokenService: TokenService) {

    private val currentRequest: HttpRequest<Any>?
    private val currentToken: String

    init {
        println("Init security context manager")
        currentRequest = ServerRequestContext.currentRequest<Any>().get()
        currentToken = extractToken(currentRequest)
    }

    private fun loadSecurityContext(): Single<Authentication> {
        return Single.just(currentToken)
            .filter { token -> token.isNotBlank() }
            .flatMap { token -> tokenService.validateTokenRx(token) }
            .flatMap { token -> getCurrentAuthentication(token).toMaybe() }
            .switchIfEmpty(Single.just(DefaultAuthentication()))
    }

    fun verifyUserId(userId: String): Single<Authentication> {
        return loadSecurityContext()
            .map { authentication ->
                if (authentication.isAuthenticated() && userId == (authentication as UserAuthentication).principal) {
                    authentication
                } else {
                    throw SecurityContextException("User [$userId] can't be accessed with the given token [$currentToken]")
                }
            }
    }

    private fun extractToken(currentRequest: HttpRequest<Any>?): String {
        val authHeader: String? = currentRequest!!.headers.authorization?.orElseGet { "" }
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
