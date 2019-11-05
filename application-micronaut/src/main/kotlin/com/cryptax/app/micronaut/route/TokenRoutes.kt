package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.model.GetTokenRequest
import com.cryptax.app.micronaut.model.GetTokenResponse
import com.cryptax.app.micronaut.web.extractToken
import com.cryptax.controller.UserController
import com.cryptax.jwt.TokenService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.context.ServerRequestContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Controller
class TokenRoutes(
    private val userController: UserController,
    private val tokenService: TokenService) {

    @Post("/token")
    fun obtainToken(@Body getTokenRequest: GetTokenRequest): Single<GetTokenResponse> {
        return userController
            .login(getTokenRequest.email, getTokenRequest.password)
            .flatMap { userWeb -> tokenService.buildToken(userWeb.id) }
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
            .subscribeOn(Schedulers.computation())
    }

    @Get("/refresh")
    fun obtainRefreshToken(): Single<GetTokenResponse> {
        return tokenService.buildTokenFromRefresh(extractToken(ServerRequestContext.currentRequest<Any>().get()))
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
            .subscribeOn(Schedulers.computation())
    }
}
