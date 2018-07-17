package com.cryptax.domain.port

import com.cryptax.domain.entity.User

interface SecurePassword {

    fun securePassword(password: CharArray): String

    fun matchPassword(challengingPassword: CharArray, hashedSaltPassword: CharArray): Boolean

    fun generateToken(user: User): String
}
