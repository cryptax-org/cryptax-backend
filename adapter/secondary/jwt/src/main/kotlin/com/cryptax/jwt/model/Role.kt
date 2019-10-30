package com.cryptax.jwt.model

enum class Role {
    ADMIN, USER;

    fun authority(): String {
        return this.name
    }
}

