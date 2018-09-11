package com.cryptax.app.route

import com.cryptax.app.jwt.JwtTokenProvider
import com.cryptax.app.model.GetTokenRequest
import com.cryptax.app.model.GetTokenResponse
import com.cryptax.controller.UserController
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenRoutes @Autowired constructor(private val userController: UserController, private val jwtTokenProvider: JwtTokenProvider) {

    @PostMapping("/token")
    fun obtainToken(@RequestBody @Validated getTokenRequest: GetTokenRequest): Single<GetTokenResponse> {
        return userController
            .login(getTokenRequest.email, getTokenRequest.password)
            .subscribeOn(Schedulers.io())
            .flatMap { userWeb -> jwtTokenProvider.buildToken(userWeb.id) }
            .map { triple -> GetTokenResponse(id = triple.first, token = triple.second, refreshToken = triple.third) }
    }

    @GetMapping("/refresh")
    fun obtainRefreshToken(req: ServerHttpRequest): Single<GetTokenResponse> {
        return jwtTokenProvider
            .buildTokenFromRefresh(req)
            .map { tripe -> GetTokenResponse(id = tripe.first, token = tripe.second, refreshToken = tripe.third) }
    }
}
