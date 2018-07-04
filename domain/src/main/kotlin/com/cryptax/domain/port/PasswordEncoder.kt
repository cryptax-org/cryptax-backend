package com.cryptax.domain.port

interface PasswordEncoder {

    fun encode(str: String): String
}
