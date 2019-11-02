package com.cryptax.app.micronaut.security

import com.cryptax.jwt.TokenService
import io.jsonwebtoken.JwtException
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.http.scope.RequestScope
import io.reactivex.Single

@RequestScope
class SecurityContext(val authentication: Authentication) {

}
