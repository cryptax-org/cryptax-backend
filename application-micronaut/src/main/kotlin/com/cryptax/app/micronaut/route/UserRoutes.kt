package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.model.ResetPasswordRequest
import com.cryptax.app.micronaut.security.SecurityContextManager
import com.cryptax.controller.UserController
import com.cryptax.controller.model.ResetPasswordWeb
import com.cryptax.controller.model.UserWeb
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.reactivex.Single
import javax.validation.Valid

@Controller("/users")
class UserRoutes(private val userController: UserController, private val service: SecurityContextManager) : Routes {

    @Post
    fun createUser(@Body userWeb: UserWeb): Single<HttpResponse<UserWeb>> {
        return userController
            .createUser(userWeb)
            .map { pair -> HttpResponse.ok<UserWeb>().header("welcomeToken", pair.second).body(pair.first) }
    }

    @Get("/{userId}/allow")
    fun allowUser(@PathVariable userId: String, @QueryValue(value = "token") token: String): Single<HttpResponse<Any>> {
        return userController
            .allowUser(userId, token)
            .map { isAllowed -> if (isAllowed) HttpResponse.ok<Any>() else HttpResponse.badRequest<Any>() }
    }

    @Get("/{userId}")
    fun getUser(@PathVariable userId: String): Single<UserWeb> {
        return service.verifyUserId(userId).flatMap {
            val res = userController.findUser(userId)
            res.toSingle()
        }
    }

    @Get("/email/{email}")
    fun sendWelcomeEmail(@PathVariable email: String): Single<Unit> {
        return userController.sendWelcomeEmail(email)
    }

    @Get("/email/{email}/reset")
    fun initiateResetPassword(@PathVariable email: String): Single<ResetPasswordWeb> {
        return userController.initiatePasswordReset(email)
    }

    @Put("/password")
    fun resetPassword(@Body body: ResetPasswordRequest): Single<Unit> {
        return userController.resetPassword(body.email, body.password, body.token)
    }
}
