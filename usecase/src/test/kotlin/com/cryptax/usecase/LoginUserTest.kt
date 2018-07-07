package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.NotAllowedException
import com.cryptax.domain.port.PasswordEncoder
import com.cryptax.domain.port.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase login users")
@ExtendWith(MockitoExtension::class)
class LoginUserTest {

	@Mock
	lateinit var userRepository: UserRepository
	@Mock
	lateinit var passwordEncoder: PasswordEncoder
	@InjectMocks
	lateinit var loginUser: LoginUser

	private val id = "1"
	private val email = "john.doe@proton.com"
	private val password = "mypassword"
	private val hashedPassword = "hashedPassword"
	private val user = User(id, "john.doe@proton.com", hashedPassword.toCharArray(), "Doe", "John")

	@Test
	@DisplayName("Login a user")
	fun testLogin() {
		//given
		given(userRepository.findByEmail(email)).willReturn(user)
		given(passwordEncoder.encode(email + password)).willReturn(hashedPassword)

		//when
		val actual = loginUser.login(email, password.toCharArray())

		//then
		assertEquals(user, actual)
		then(userRepository).should().findByEmail(email)
		then(passwordEncoder).should().encode(email + password)
	}

	@Test
	@DisplayName("Login a user not found")
	fun testLoginUserNotFound() {
		//given
		given(userRepository.findByEmail(email)).willReturn(null)

		//when
		val exception = assertThrows(NotAllowedException::class.java) {
			loginUser.login(email, password.toCharArray())
		}

		//then
		assertEquals("Not allowed", exception.message)
		then(userRepository).should().findByEmail(email)
	}

	@Test
	@DisplayName("Login a user wrong password")
	fun testLoginWrongPassword() {
		//given
		given(userRepository.findByEmail(email)).willReturn(user)
		given(passwordEncoder.encode(email + "wrong password")).willReturn("wrong hashed password")

		//when
		val exception = assertThrows(NotAllowedException::class.java) {
			loginUser.login(email, "wrong password".toCharArray())
		}

		//then
		assertEquals("Not allowed", exception.message)
		then(userRepository).should().findByEmail(email)
	}
}
