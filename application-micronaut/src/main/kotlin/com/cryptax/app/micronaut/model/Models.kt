package com.cryptax.app.micronaut.model

import javax.validation.constraints.NotEmpty

data class ErrorResponse(val error: String, val details: List<String>? = null)

class GetTokenRequest {
    @get:NotEmpty(message = "{token.email}")
    var email: String? = null
    @get:NotEmpty(message = "{token.password}")
    var password: CharArray? = null
}

data class GetTokenResponse(val id: String, val token: String, val refreshToken: String)

class ResetPasswordRequest(
    val email: String? = null,
    val password: CharArray? = null,
    val token: String? = null)
