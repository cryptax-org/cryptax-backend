package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.model.GetTokenRequest
import com.cryptax.app.micronaut.model.GetTokenResponse
import com.cryptax.app.micronaut.security.SecurityContextManager
import com.cryptax.controller.UserController
import com.cryptax.jwt.TokenService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Controller
class TokenRoutes(private val userController: UserController, private val tokenService: TokenService) {

    @Inject
    internal var securityContextManager: SecurityContextManager? = null

    @Post("/token")
    fun obtainToken(@Body /*@Valid*/ getTokenRequest: GetTokenRequest): Single<GetTokenResponse> {
        return userController
            .login(getTokenRequest.email!!, getTokenRequest.password!!)
            .subscribeOn(Schedulers.computation())
            .flatMap { userWeb -> tokenService.buildToken(userWeb.id) }
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
    }

    @Get("/refresh")
    fun obtainRefreshToken(@Header authHeader: String): Single<GetTokenResponse> {
        return tokenService
            .buildTokenFromRefresh(extractToken(authHeader))
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
    }

    private fun extractToken(authHeader: String): String {
        return if (authHeader.isNotBlank() && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        } else ""
    }
}
