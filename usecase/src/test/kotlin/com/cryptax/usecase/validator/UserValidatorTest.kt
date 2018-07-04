package com.cryptax.usecase.validator

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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


	@ParameterizedTest
	@MethodSource("userProvider")
	fun userProvider(user: User?, errorMessage: String) {
		//when
		val exception = assertThrows(UserValidationException::class.java) {
			UserValidator.validateCreateUser(user)
		}

		//then
		assertEquals(errorMessage, exception.message)
	}

	companion object {

		@JvmStatic
		fun userProvider(): Stream<Arguments> {
			return Stream.of(
				Arguments.of(null, "User should not be null"),
				Arguments.of(User("eeqqqw", "", "eeeee", "ee", "ee"), "Email should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee", "ee", ""), "First name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee", "", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee", "      ", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee", " ", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee", "				", "eqweqwe"), "Last name should not be blank")
			)
		}
	}
}
