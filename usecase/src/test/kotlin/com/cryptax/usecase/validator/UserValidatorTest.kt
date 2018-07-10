package com.cryptax.usecase.validator

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserValidationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@DisplayName("User data validation")
class UserValidatorTest {

	@Test
	fun testValidateCreateUser() {
		//given
		val user = User("eeqqqw", "eeee", "eeeee".toCharArray(), "ee", "ee")

		//when
		validateCreateUser(user)

		//then
		// no failure
	}

	@ParameterizedTest
	@MethodSource("userProvider")
	fun testValidateCreateUserFail(user: User, errorMessage: String) {
		//when
		val exception = Assertions.assertThrows(UserValidationException::class.java) {
			validateCreateUser(user)
		}

		//then
		Assertions.assertEquals(errorMessage, exception.message)
	}

	companion object {

		@JvmStatic
		fun userProvider(): Stream<Arguments> {
			return Stream.of(
				Arguments.of(User("eeqqqw", "", "eeeee".toCharArray(), "ee", "ee"), "Email should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee".toCharArray(), "ee", ""), "First name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee".toCharArray(), "", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee".toCharArray(), "      ", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee".toCharArray(), " ", "eqweqwe"), "Last name should not be blank"),
				Arguments.of(User("eeqqqw", "dqwdqdq", "eeeee".toCharArray(), "				", "eqweqwe"), "Last name should not be blank")
			)
		}
	}
}
