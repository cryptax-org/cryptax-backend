package com.cryptax.app.micronaut.security

interface Authentication {
    fun isAuthenticated(): Boolean
}

class DefaultAuthentication : Authentication {
    override fun isAuthenticated(): Boolean {
        return false
    }
}

data class UserAuthentication(val principal: String, val token: String, val authorities: List<String>) : Authentication {
    override fun isAuthenticated(): Boolean {
        return true
    }
}
