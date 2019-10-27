package com.cryptax.app.route

import com.cryptax.app.jwt.JwtTokenProvider
import com.cryptax.app.model.GetTokenRequest
import com.cryptax.app.model.GetTokenResponse
import com.cryptax.controller.UserController
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Controller
class TokenRoutes(private val userController: UserController, private val jwtTokenProvider: JwtTokenProvider) {

    @Post("/token")
    fun obtainToken(@Body /*@Valid*/ getTokenRequest: GetTokenRequest): Single<GetTokenResponse> {
        return userController
            .login(getTokenRequest.email!!, getTokenRequest.password!!)
            .subscribeOn(Schedulers.computation())
            .flatMap { userWeb -> jwtTokenProvider.buildToken(userWeb.id!!) }
            .map { triple -> GetTokenResponse(id = triple.first, token = triple.second, refreshToken = triple.third) }
    }

    @Get("/refresh")
    fun obtainRefreshToken(req: HttpRequest<String>): Single<GetTokenResponse> {
        return jwtTokenProvider
            .buildTokenFromRefresh(req)
            .map { tripe -> GetTokenResponse(id = tripe.first, token = tripe.second, refreshToken = tripe.third) }
    }
}
