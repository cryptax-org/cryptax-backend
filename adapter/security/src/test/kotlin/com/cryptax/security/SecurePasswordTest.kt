package com.cryptax.security

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

@DisplayName("Password security tests")
class SecurePasswordTest {

	private val securePassword = SecurePassword()

	@TestFactory
	fun testSecurePassword(): List<DynamicTest> {
		return PASSWORDS.map { password ->
			DynamicTest.dynamicTest("Test secure password ${password.joinToString("")}") {
				val actual = securePassword.securePassword(password)
				assert(actual.contains('_'))
			}
		}
	}

	@TestFactory
	fun testMatchPassword(): List<DynamicTest> {
		return PASSWORDS.map { password ->
			DynamicTest.dynamicTest("Test match password ${password.joinToString("")}") {
				// given
				val hashedSaltPassword = securePassword.securePassword(password)

				//when
				val actual = securePassword.matchPassword(password, hashedSaltPassword.toCharArray())

				//then
				assert(actual)
			}
		}
	}

	companion object {
		private val PASSWORDS = listOf(
			"mypassword".toCharArray(),
			"derp".toCharArray(),
			"273891273hkljhfklqj212```".toCharArray(),
			"test".toCharArray(), "0".toCharArray(),
			"qwerty".toCharArray(),
			"/01-01-=`".toCharArray(),
			"55vcFjkljklfjWFEDj".toCharArray(),
			"FRANCE".toCharArray()
		)
	}
}
