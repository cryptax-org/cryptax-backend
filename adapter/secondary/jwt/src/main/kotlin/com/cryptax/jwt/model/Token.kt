package com.cryptax.jwt.model

data class Token(
    val userId: String,
    val token: String,
    val refresh: String
)
