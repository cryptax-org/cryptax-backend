package com.cryptax.controller.validation

import com.cryptax.controller.model.ResetPasswordWeb
import com.cryptax.controller.model.UserWeb
import io.reactivex.Single

object UserValidation {

    fun validateCreateUser(userWeb: UserWeb): Single<UserWeb> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (userWeb.email.isBlank()) {
                errors.add("Email can not be empty")
            }
            if (userWeb.password == null || userWeb.password.isEmpty()) {
                errors.add("Password can not be empty")
            }
            if (userWeb.lastName.isBlank()) {
                errors.add("Last name can not be empty")
            }
            if (userWeb.firstName.isBlank()) {
                errors.add("First name can not be empty")
            }
            if (errors.isEmpty()) {
                userWeb
            } else {
                throw ValidationException(errors)
            }
        }
    }

    fun validateRestPassword(email: String?, password: CharArray?, token: String?): Single<Triple<String, CharArray, String>> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (email == null || email.isBlank()) {
                errors.add("Email can not be empty")
            }
            if (password == null || password.isEmpty()) {
                errors.add("Password can not be empty")
            }
            if (token == null || token.isBlank()) {
                errors.add("Token can not be empty")
            }
            if (errors.isEmpty()) {
                Triple(email!!, password!!, token!!)
            } else {
                throw ValidationException(errors)
            }
        }
    }
}

class ValidationException(val errors: List<String>) : RuntimeException()
