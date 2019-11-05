package com.cryptax.controller

import com.cryptax.controller.model.ResetPasswordWeb
import com.cryptax.controller.model.UserWeb
import com.cryptax.controller.validation.Validation
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ResetUserPassword
import com.cryptax.usecase.user.ValidateUser
import io.reactivex.Maybe
import io.reactivex.Single

class UserController(
    private val createUser: CreateUser,
    private val findUser: FindUser,
    private val loginUser: LoginUser,
    private val validateUser: ValidateUser,
    private val resetUserPassword: ResetUserPassword) {

    fun createUser(userWeb: UserWeb): Single<Pair<UserWeb, String>> {
        return Validation.validateCreateUser(userWeb)
            .flatMap { user -> createUser.create(user.toUser()) }
            .map { pair -> Pair(UserWeb.toUserWeb(pair.first), pair.second) }
    }

    fun login(email: String?, password: CharArray?): Single<UserWeb> {
        return Validation.validateLogin(email, password)
            .flatMap { pair -> loginUser.login(pair.first, pair.second) }
            .map { user -> UserWeb.toUserWeb(user) }
    }

    fun findUser(userId: String): Maybe<UserWeb> {
        return findUser.findById(userId)
            .map { user -> UserWeb.toUserWeb(user) }
    }

    fun allowUser(userId: String, token: String): Single<Boolean> {
        return validateUser.validate(userId, token)
    }

    fun sendWelcomeEmail(userId: String): Single<Unit> {
        return createUser.sendWelcomeEmail(userId).map { Unit }
    }

    fun initiatePasswordReset(email: String): Single<ResetPasswordWeb> {
        return resetUserPassword.initiatePasswordReset(email)
            .map { resetPassword -> ResetPasswordWeb(resetPassword.token) }
    }

    fun resetPassword(email: String?, password: CharArray?, token: String?): Single<Unit> {
        return Validation.validateRestPassword(email, password, token)
            .flatMap { triple -> resetUserPassword.resetPassword(triple.first, triple.second, triple.third) }
    }
}
