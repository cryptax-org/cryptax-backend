package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ValidateUser
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class UserController(
    private val createUser: CreateUser,
    private val findUser: FindUser,
    private val loginUser: LoginUser,
    private val validateUser: ValidateUser) {

    fun createUser(userWeb: UserWeb): Single<Pair<UserWeb, String>> {
        return createUser
            .create(userWeb.toUser())
            .map { pair -> Pair(UserWeb.toUserWeb(pair.first), pair.second) }
            .subscribeOn(Schedulers.io())
    }

    fun login(email: String, password: CharArray): Single<UserWeb> {
        return loginUser
            .login(email, password)
            .map { user -> UserWeb.toUserWeb(user) }
            .subscribeOn(Schedulers.io())
    }

    fun findUser(userId: String): UserWeb? {
        val user = findUser.findById(userId)
        return if (user != null) {
            UserWeb.toUserWeb(user)
        } else {
            null
        }
    }

    fun allowUser(userId: String, token: String): Boolean {
        return validateUser.validate(userId, token)
    }
}
