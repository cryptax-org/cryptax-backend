package com.cryptax.app.micronaut.model

data class ErrorResponse(val error: String, val details: List<String>? = null)
data class GetTokenRequest(val email: String? = null, val password: CharArray? = null)
data class GetTokenResponse(val id: String, val token: String, val refreshToken: String)
data class ResetPasswordRequest(val email: String? = null, val password: CharArray? = null, val token: String? = null)
