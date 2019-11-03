package com.cryptax.app.micronaut.route

import com.cryptax.controller.UserController
import com.cryptax.controller.model.UserWeb
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.reactivex.Single

@Controller
class HelloController(val userController: UserController) {

    @Get(value = "/hello", produces = [MediaType.TEXT_PLAIN])
    fun index(): String {
        return "Hello World"
    }

    @Post(value="/derp", produces = [MediaType.TEXT_PLAIN])
    fun derp(@Body d: UserWeb): Single<String> {
        println(d)
        val user = userController
            .createUser(d)
            .map { pair -> HttpResponse.ok<UserWeb>().header("welcomeToken", pair.second).body(pair.first) }
            .blockingGet()
        println(d)
        println(user)
        return Single.just("Hello World")
    }
}
