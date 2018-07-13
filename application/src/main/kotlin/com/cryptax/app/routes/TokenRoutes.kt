package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.config.Config
import com.cryptax.validation.RestValidation
import com.cryptax.validation.RestValidation.loginValidation
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router

fun handleTokenRoutes(config: Config, router: Router, jwtProvider: JWTAuth) {

    val userController = config.userController()

    // Get token with user credentials
    router.post("/token")
        .handler(RestValidation.jsonContentTypeValidation)
        .handler(bodyHandler)
        .handler(loginValidation)
        .handler { routingContext ->
            val userWeb = userController.login(
                email = routingContext.bodyAsJson.getString("email"),
                password = routingContext.bodyAsJson.getString("password").toCharArray())

            val result = JsonObject().put("id", userWeb.id)
            val token = jwtProvider.generateToken(result, Config.jwtOptions)
            result.put("token", token)
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)
}
