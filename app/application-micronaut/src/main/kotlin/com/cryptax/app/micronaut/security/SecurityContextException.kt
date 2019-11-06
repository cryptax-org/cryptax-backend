package com.cryptax.app.micronaut.security

class SecurityContextException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable) : super(message, exception)
}
