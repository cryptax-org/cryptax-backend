package com.cryptax.app.route

import com.cryptax.controller.UserController
import com.cryptax.controller.model.UserWeb
import com.cryptax.controller.validation.Create
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRoutes @Autowired constructor(private val userController: UserController) {

    @PostMapping("/users")
    fun createUser(@RequestBody @Validated(value = [Create::class]) userWeb: UserWeb): ResponseEntity<UserWeb> {
        val pair = userController
            .createUser(userWeb)
            .subscribeOn(Schedulers.io())
            .blockingGet()
        return ResponseEntity.ok().header("welcomeToken", pair.second).body(pair.first)
    }
}
