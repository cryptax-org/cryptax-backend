package com.cryptax.app.route

import com.cryptax.app.model.ResetPasswordRequest
import com.cryptax.controller.UserController
import com.cryptax.controller.model.ResetPasswordWeb
import com.cryptax.controller.model.UserWeb
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.reactivex.Single
import reactor.adapter.rxjava.toMono
import reactor.core.publisher.Mono
import javax.validation.Valid

@Controller("/users")
class UserRoutes(private val userController: UserController) : Routes {

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
    fun getUser(@PathVariable userId: String): Mono<UserWeb> {
        return verifyUserId(userId).flatMap { boolean ->
            val res = userController.findUser(userId).toMono()
            res
        }
    }

    @Get("/email/{email}")
    fun sendWelcomeEmail(@PathVariable email: String): Single<Unit> {
        return userController.sendWelcomeEmail(email)
    }

    @Get("/email/{email}/reset")
    private fun initiateResetPassword(@PathVariable email: String): Single<ResetPasswordWeb> {
        return userController.initiatePasswordReset(email)
    }

    @Put("/password")
    private fun resetPassword(@Body @Valid body: ResetPasswordRequest): Single<Unit> {
        return userController.resetPassword(body.email!!, body.password!!, body.token!!)
    }
}
