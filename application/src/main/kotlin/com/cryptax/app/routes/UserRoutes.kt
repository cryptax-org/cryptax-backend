package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.controller.UserController
import com.cryptax.controller.model.UserWeb
import com.cryptax.validation.RestValidation.allowUserValidation
import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.sendWelcomeEmailValidation
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleUserRoutes(router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler, userController: UserController) {

    // Create user
    router.post("/users")
        .handler(jsonContentTypeValidation)
        .handler(bodyHandler)
        .handler(createUserValidation)
        .handler { routingContext ->
            val userWeb = routingContext.body.toJsonObject().mapTo(UserWeb::class.java)
            userController
                .createUser(userWeb)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { pair ->
                        routingContext.response().putHeader("welcomeToken", pair.second)
                        sendSuccess(JsonObject.mapFrom(pair.first), routingContext.response())
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Get user with user id and JWT token
    router.get("/users/:userId")
        .handler(jwtAuthHandler)
        .handler(getUserValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            // Previous validation 'insure' (95%) the user exists
            userController
                .findUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { user ->
                        val result = JsonObject.mapFrom(user)
                        sendSuccess(result, routingContext.response())
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Resend welcome email containing the welcome token
    router.get("/users/email/:email")
        .handler(sendWelcomeEmailValidation)
        .handler { routingContext ->
            val email = routingContext.request().getParam("email")
            userController
                .sendWelcomeEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { _ -> routingContext.response().setStatusCode(200).end() },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Allow user to login after email validation
    router.get("/users/:userId/allow")
        .handler(allowUserValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val token = routingContext.request().getParam("token")
            userController
                .allowUser(userId, token)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { isAllowed ->
                        routingContext.response()
                            .setStatusCode(if (isAllowed) 200 else 400)
                            .end()
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)
}

