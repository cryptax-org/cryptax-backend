package com.cryptax.app.jwt

class JwtException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, throwable: Throwable) : super(message, throwable)
}
