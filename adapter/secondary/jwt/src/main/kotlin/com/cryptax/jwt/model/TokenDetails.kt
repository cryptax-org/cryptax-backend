package com.cryptax.jwt.model

data class TokenDetails(
    val subject: String,
    val roles: List<Role>
)
