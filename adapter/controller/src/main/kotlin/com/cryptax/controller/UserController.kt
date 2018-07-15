package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser

class UserController(private val createUser: CreateUser, private val findUser: FindUser, private val loginUser: LoginUser) {

    fun createUser(userWeb: UserWeb): UserWeb {
        val user = createUser.create(userWeb.toUser())
        return UserWeb.toUserWeb(user)
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
}
