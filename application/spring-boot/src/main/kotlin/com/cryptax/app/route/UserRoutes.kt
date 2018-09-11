package com.cryptax.app.route

import com.cryptax.controller.UserController
import com.cryptax.controller.model.UserWeb
import com.cryptax.controller.validation.Create
import io.reactivex.Maybe
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/users")
@RestController
class UserRoutes @Autowired constructor(private val userController: UserController) {

    @PostMapping
    fun createUser(@RequestBody @Validated(value = [Create::class]) userWeb: UserWeb): Single<ResponseEntity<UserWeb>> {
        return userController
            .createUser(userWeb)
            .map { pair -> ResponseEntity.ok().header("welcomeToken", pair.second).body(pair.first) }
    }

    @GetMapping("/{userId}/allow", params = ["token"])
    fun allowUser(@PathVariable userId: String, @RequestParam("token") token: String): Single<ResponseEntity<Any>> {
        return userController
            .allowUser(userId, token)
            .map { isAllowed -> ResponseEntity<Any>(HttpStatus.valueOf(if (isAllowed) HttpStatus.OK.value() else HttpStatus.BAD_REQUEST.value())) }
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): Maybe<UserWeb> {
        return userController.findUser(userId)
    }
}
