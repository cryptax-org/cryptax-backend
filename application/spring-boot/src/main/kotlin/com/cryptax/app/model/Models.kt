package com.cryptax.app.model

import java.util.Arrays
import javax.validation.constraints.NotEmpty

data class GetTokenRequest(
    @get:NotEmpty(message = "Email can not be empty")
    val email: String,
    @get:NotEmpty(message = "Password can not be empty")
    val password: CharArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetTokenRequest

        if (email != other.email) return false
        if (!Arrays.equals(password, other.password)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + Arrays.hashCode(password)
        return result
    }
}

data class GetTokenResponse(val id: String, val token: String, val refreshToken: String)
