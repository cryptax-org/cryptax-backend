package com.cryptax.domain.entity

import java.util.Arrays

data class User(
    val id: String? = null,
    val email: String,
    val password: CharArray,
    val lastName: String,
    val firstName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (email != other.email) return false
        if (!Arrays.equals(password, other.password)) return false
        if (lastName != other.lastName) return false
        if (firstName != other.firstName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + email.hashCode()
        result = 31 * result + Arrays.hashCode(password)
        result = 31 * result + lastName.hashCode()
        result = 31 * result + firstName.hashCode()
        return result
    }
}
