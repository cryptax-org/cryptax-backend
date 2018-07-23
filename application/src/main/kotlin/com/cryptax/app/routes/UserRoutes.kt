package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.config.AppConfig
import com.cryptax.controller.model.UserWeb
import com.cryptax.validation.RestValidation.allowUserValidation
import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import io.reactivex.Scheduler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

private val log: Logger = LoggerFactory.getLogger("handleUserRoutes")

fun handleUserRoutes(appConfig: AppConfig, router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler) {

    val userController = appConfig.userController

    // Create user
    router.post("/users")
        .handler(jsonContentTypeValidation)
        .handler(bodyHandler)
        .handler(createUserValidation)
        .handler { routingContext ->
            val userWeb = routingContext.body.toJsonObject().mapTo(UserWeb::class.java)
            userController
                .createUser(userWeb)
                .observeOn(vertxScheduler)
                .doOnError {
                    routingContext.fail(it)
                }
                .subscribe { pair ->
                    routingContext.response().putHeader("welcomeToken", pair.second)
                    sendSuccess(JsonObject.mapFrom(pair.first), routingContext.response())
                }
        }
        .failureHandler(failureHandler)

    // Get user with user id and JWT token
    router.get("/users/:userId")
        .handler(jwtAuthHandler)
        .handler(getUserValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            // Previous validation 'insure' (95%) the user exists
            val userWeb = userController.findUser(userId)!!
            val result = JsonObject.mapFrom(userWeb)
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)

    // Allow user to login after email validation
    router.get("/users/:userId/allow")
        .handler(allowUserValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val token = routingContext.request().getParam("token")
            val isAllowed = userController.allowUser(userId, token)
            routingContext.response()
                .setStatusCode(if (isAllowed) 200 else 400)
                .end()
        }
}

