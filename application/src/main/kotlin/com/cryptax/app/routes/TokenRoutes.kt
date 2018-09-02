package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.config.Config
import com.cryptax.controller.UserController
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.loginValidation
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.kotlin.ext.auth.jwt.JWTOptions

fun handleTokenRoutes(config: Config, router: Router, jwtProvider: JWTAuth, jwtRefreshAuthHandler: Routes.JWTRefreshAuthHandlerCustom, vertxScheduler: Scheduler, userController: UserController) {

    val jwtOptions = JWTOptions(algorithm = config.properties.jwt.algorithm, issuer = config.properties.jwt.issuer, expiresInMinutes = config.properties.jwt.expiresInMinutes)
    val jwtRefreshOptions = JWTOptions(algorithm = config.properties.jwt.algorithm, issuer = config.properties.jwt.issuer, expiresInMinutes = config.properties.jwt.refreshExpiresInDays * 1440)

    // Get token with user credentials
    router.post("/token")
        .handler(jsonContentTypeValidation)
        .handler(bodyHandler)
        .handler(loginValidation)
        .handler { routingContext ->
            userController.login(
                email = routingContext.bodyAsJson.getString("email"),
                password = routingContext.bodyAsJson.getString("password").toCharArray())
                .observeOn(vertxScheduler)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { userWeb ->
                        val token = jwtProvider.generateToken(tokenPayLoad(userWeb.id, false), jwtOptions)
                        val refreshToken = jwtProvider.generateToken(tokenPayLoad(userWeb.id, true), jwtRefreshOptions)

                        val result = JsonObject()
                            .put("id", userWeb.id)
                            .put("token", token)
                            .put("refreshToken", refreshToken)
                        sendSuccess(result, routingContext.response())
                    },
                    { error -> routingContext.fail(error) }
                )
        }
        .failureHandler(failureHandler)

    // Obtain a new token from a refresh token
    router.get("/refresh")
        .handler(jwtRefreshAuthHandler)
        .handler { routingContext ->
            val userId = routingContext.user().principal().getString("id")

            val token = jwtProvider.generateToken(tokenPayLoad(userId, false), jwtOptions)
            val refreshToken = jwtProvider.generateToken(tokenPayLoad(userId, true), jwtRefreshOptions)
            val result = JsonObject()
                .put("id", userId)
                .put("token", token)
                .put("refreshToken", refreshToken)
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)
}

private fun tokenPayLoad(id: String, isRefresh: Boolean): JsonObject {
    return JsonObject().put("id", id).put("isRefresh", isRefresh)
}
