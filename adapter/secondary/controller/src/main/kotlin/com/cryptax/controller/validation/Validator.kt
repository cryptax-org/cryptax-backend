package com.cryptax.controller.validation

import com.cryptax.controller.exception.ControllerValidationException
import com.cryptax.controller.model.UserWeb
import io.reactivex.Single

class Validator {
    fun validateCreateUser(user: UserWeb): Single<UserWeb> {
        return Single.fromCallable {
            val violations = mutableListOf<Violation>()
            if (user.email.isBlank()) violations.add(Violation("Email can not be empty"))
            if (user.password == null || user.password.isEmpty()) violations.add(Violation("Password can not be empty"))
            if (user.lastName.isBlank()) violations.add(Violation("Last name can not be empty"))
            if (user.email.isBlank()) violations.add(Violation("First name can not be empty"))
            if (violations.isNotEmpty()) {
                throw ControllerValidationException(violations)
            }
            user
        }
    }
}

class Violation(val message: String)
