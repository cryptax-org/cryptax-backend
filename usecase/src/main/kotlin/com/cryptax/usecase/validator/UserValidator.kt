package com.cryptax.usecase.validator

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserValidationException

object UserValidator {

	fun validateCreateUser(user: User?) {
		if (user == null) throw UserValidationException("User should not be null")
		if (user.email.isBlank()) throw UserValidationException("Email should not be blank")
		if (user.firstName.isBlank()) throw UserValidationException("First name should not be blank")
		if (user.lastName.isBlank()) throw UserValidationException("Last name should not be blank")
	}
}
