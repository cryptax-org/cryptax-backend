package com.cryptax.domain.port

interface SecurePassword {

	fun securePassword(password: CharArray): String

	fun matchPassword(challengingPassword: CharArray, hashedSaltPassword: CharArray): Boolean
}
