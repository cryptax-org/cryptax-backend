package com.cryptax.app.model

import javax.validation.constraints.NotEmpty

data class GetTokenRequest(
    @get:NotEmpty(message = "Email can not be empty")
    val email: String,
    @get:NotEmpty(message = "Password can not be empty")
    val password: CharArray
)

data class GetTokenResponse(val id: String, val token: String, val refreshToken: String)

data class ResetPasswordRequest(
    @get:NotEmpty(message = "Email can not be empty")
    val email: String,
    @get:NotEmpty(message = "Password can not be empty")
    val password: CharArray,
    @get:NotEmpty(message = "Token can not be empty")
    val token: String)
