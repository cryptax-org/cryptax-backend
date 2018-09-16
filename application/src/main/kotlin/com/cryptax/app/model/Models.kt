package com.cryptax.app.model

import javax.validation.constraints.NotEmpty

class GetTokenRequest() {
    @get:NotEmpty(message = "Email can not be empty")
    var email: String? = null
    @get:NotEmpty(message = "Password can not be empty")
    var password: CharArray? = null
}

data class GetTokenResponse(val id: String, val token: String, val refreshToken: String)

class ResetPasswordRequest() {
    @get:NotEmpty(message = "Email can not be empty")
    var email: String? = null
    @get:NotEmpty(message = "Password can not be empty")
    var password: CharArray? = null
    @get:NotEmpty(message = "Token can not be empty")
    var token: String? = null
}
