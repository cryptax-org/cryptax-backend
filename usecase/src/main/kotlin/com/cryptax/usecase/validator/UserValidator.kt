package com.cryptax.usecase.validator

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserValidationException
import org.apache.commons.lang3.StringUtils.isBlank

object UserValidator {

	fun validateCreateUser(user: User?) {
		if (user == null) throw UserValidationException("User should not be null")
		if (isBlank(user.email)) throw UserValidationException("Email should not be null")
		if (isBlank(user.firstName)) throw UserValidationException("First name should not be null")
		if (isBlank(user.lastName)) throw UserValidationException("Last name should not be null")
	}
}
