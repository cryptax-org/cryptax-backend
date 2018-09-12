package com.cryptax.app.route

import com.cryptax.app.jwt.JwtException
import com.cryptax.app.model.ResetPasswordRequest
import com.cryptax.controller.UserController
import com.cryptax.controller.model.ResetPasswordWeb
import com.cryptax.controller.model.UserWeb
import com.cryptax.controller.validation.Create
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.adapter.rxjava.toMono
import reactor.core.publisher.Mono

@RequestMapping("/users")
@RestController
class UserRoutes @Autowired constructor(private val userController: UserController) {

    @PostMapping
    fun createUser(@RequestBody @Validated(value = [Create::class]) userWeb: UserWeb): Single<ResponseEntity<UserWeb>> {
        return userController
            .createUser(userWeb)
            .map { pair -> ResponseEntity.ok().header("welcomeToken", pair.second).body(pair.first) }
    }

    @GetMapping("/{userId}/allow")
    fun allowUser(@PathVariable userId: String, @RequestParam(value = "token", required = true) token: String): Single<ResponseEntity<Any>> {
        return userController
            .allowUser(userId, token)
            .map { isAllowed -> ResponseEntity<Any>(if (isAllowed) HttpStatus.OK else HttpStatus.BAD_REQUEST) }
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): Mono<UserWeb> {
        return verifyUserId(userId).flatMap { userController.findUser(userId).toMono() }
    }

    @GetMapping("/email/{email}")
    fun sendWelcomeEmail(@PathVariable email: String): Single<Unit> {
        return userController.sendWelcomeEmail(email)
    }

    @GetMapping("/email/{email}/reset")
    fun initiateResetPassword(@PathVariable email: String): Single<ResetPasswordWeb> {
        return userController.initiatePasswordReset(email)
    }

    @PutMapping("/password")
    fun resetPassword(@RequestBody @Validated body: ResetPasswordRequest): Single<Unit> {
        return userController.resetPassword(body.email, body.password, body.token)
    }

    private fun verifyUserId(userId: String): Mono<Boolean> {
        return ReactiveSecurityContextHolder
            .getContext()
            .flatMap { context ->
                when (context.authentication.principal as String) {
                    userId -> Mono.just(true)
                    else -> Mono.error(JwtException("User $userId can't be accessed with the given token ${context.authentication.credentials}"))
                }
            }
    }
}
