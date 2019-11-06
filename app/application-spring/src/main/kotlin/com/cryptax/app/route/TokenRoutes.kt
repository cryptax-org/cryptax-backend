package com.cryptax.app.route

import com.cryptax.app.jwt.extractToken
import com.cryptax.app.model.GetTokenRequest
import com.cryptax.app.model.GetTokenResponse
import com.cryptax.controller.UserController
import com.cryptax.jwt.TokenService
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
class TokenRoutes @Autowired constructor(private val userController: UserController, private val tokenService: TokenService) {

    @PostMapping("/token")
    fun obtainToken(@RequestBody @Validated getTokenRequest: GetTokenRequest): Single<GetTokenResponse> {
        return userController
            .login(getTokenRequest.email!!, getTokenRequest.password!!)
            .subscribeOn(Schedulers.io())
            .flatMap { userWeb -> tokenService.buildToken(userWeb.id) }
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
    }

    @GetMapping("/refresh")
    fun obtainRefreshToken(req: ServerHttpRequest): Single<GetTokenResponse> {
        return tokenService
            .buildTokenFromRefresh(extractToken(req))
            .map { token -> GetTokenResponse(id = token.userId, token = token.token, refreshToken = token.refresh) }
    }
}
