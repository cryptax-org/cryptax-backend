package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("User controller")
@ExtendWith(MockitoExtension::class)
class UserControllerTest {

	private val password = "mypassword".toCharArray()
	private val user = User(
		id = "randomid",
		email = "email@email.com",
		password = password,
		lastName = "Doe",
		firstName = "John")
	private val userWeb = UserWeb(
		email = user.email,
		password = user.password,
		lastName = user.lastName,
		firstName = user.firstName)

	@Mock
	lateinit var createUser: CreateUser
	@Mock
	lateinit var findUser: FindUser
	@Mock
	lateinit var loginUser: LoginUser
	@InjectMocks
	lateinit var userController: UserController

	@Test
	fun testCreateUser() {
		// given
		given(createUser.create(any())).willReturn(user)

		// when
		val actual = userController.createUser(userWeb)

		// then
		assertNotNull(actual.id)
		assertEquals(userWeb.email, actual.email)
		assertEquals(userWeb.lastName, actual.lastName)
		assertEquals(userWeb.firstName, actual.firstName)
		argumentCaptor<User>().apply {
			then(createUser).should().create(capture())
			assertNull(firstValue.id)
			assertEquals(userWeb.email, firstValue.email)
			assertEquals(password, firstValue.password)
			assertEquals(userWeb.lastName, firstValue.lastName)
			assertEquals(userWeb.firstName, firstValue.firstName)
		}
	}

	@Test
	fun testLoginUser() {
		// given
		val email = "email@email.com"
		val password = "mypassword".toCharArray()
		given(loginUser.login(email, password)).willReturn(user)

		// when
		val actual = userController.login(email, password)

		// then
		assertEquals(user.id, actual.id)
		assertEquals(user.email, actual.email)
		assertEquals(user.lastName, actual.lastName)
		assertEquals(user.firstName, actual.firstName)
		then(loginUser).should().login(email, password)
	}

	@Test
	fun testFindUser() {
		// given
		val userId = "random user id"
		given(findUser.findById(userId)).willReturn(user)

		// when
		val actual = userController.findUser(userId)

		// then
		assertNotNull(actual)
		assertEquals(user.id, actual!!.id)
		assertEquals(user.email, actual.email)
		assertEquals(user.lastName, actual.lastName)
		assertEquals(user.firstName, actual.firstName)
		then(findUser).should().findById(userId)
	}

	@Test
	fun testFindUserNotFound() {
		// given
		val userId = "random user id"
		given(findUser.findById(userId)).willReturn(null)

		// when
		val actual = userController.findUser(userId)

		// then
		assertNull(actual)
		then(findUser).should().findById(userId)
	}

	@Test
	fun testFindAllUsers() {
		// given
		given(findUser.findAllUsers()).willReturn(listOf(user))

		// when
		val actual = userController.findAllUsers()

		// then
		assert(actual.size == 1)
		assertEquals(user.id, actual[0].id)
		assertEquals(user.email, actual[0].email)
		assertEquals(user.lastName, actual[0].lastName)
		assertEquals(user.firstName, actual[0].firstName)
		then(findUser).should().findAllUsers()
	}
}
