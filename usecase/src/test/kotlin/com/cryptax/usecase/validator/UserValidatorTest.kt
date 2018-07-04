package com.cryptax.usecase.validator

import com.cryptax.domain.entity.User
import org.junit.jupiter.api.Test

class UserValidatorTest {

	@Test
	fun testValidateCreateUser() {
		//given
		val user = User("eeqqqw", "eeee", "eeeee", "ee", "ee")

		//when
		UserValidator.validateCreateUser(user)

		//then
		// no failure
	}
}
