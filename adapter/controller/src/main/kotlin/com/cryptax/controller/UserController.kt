package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ValidateUser

class UserController(
    private val createUser: CreateUser,
    private val findUser: FindUser,
    private val loginUser: LoginUser,
    private val validateUser: ValidateUser) {

    fun createUser(userWeb: UserWeb): Pair<UserWeb, String> {
        val user = createUser.create(userWeb.toUser())
        return Pair(UserWeb.toUserWeb(user.first), user.second)
    }

    fun login(email: String, password: CharArray): UserWeb {
        val user = loginUser.login(email, password)
        return UserWeb.toUserWeb(user)
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
