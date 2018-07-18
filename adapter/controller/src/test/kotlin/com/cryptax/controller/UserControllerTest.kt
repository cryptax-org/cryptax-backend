package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ValidateUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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

    private val welcomeToken = "dqdqwdqwd"
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
    lateinit var validateUser: ValidateUser
    @Mock
    lateinit var createUser: CreateUser
    @Mock
    lateinit var findUser: FindUser
    @Mock
    lateinit var loginUser: LoginUser
    @InjectMocks
    lateinit var userController: UserController

   /* @Test
    fun testCreateUser() {
        // given
        given(createUser.create(any())).willReturn(Pair(user, welcomeToken))

        // when
        val actual = userController.createUser(userWeb)

        // then
        val actualUser = actual.first
        val actualToken = actual.second
        assertThat(actualUser.id).isNotNull()
        assertThat(actualUser.email).isEqualTo(userWeb.email)
        assertThat(actualUser.lastName).isEqualTo(userWeb.lastName)
        assertThat(actualUser.firstName).isEqualTo(userWeb.firstName)
        assertThat(actualToken).isEqualTo(welcomeToken)
        argumentCaptor<User>().apply {
            then(createUser).should().create(capture())
            assertNull(firstValue.id)
            assertEquals(userWeb.email, firstValue.email)
            assertEquals(password, firstValue.password)
            assertEquals(userWeb.lastName, firstValue.lastName)
            assertEquals(userWeb.firstName, firstValue.firstName)
        }
    }*/

/*    @Test
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
    }*/

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
    fun testAllowUser() {
        // given
        val userId = "userId"
        val token = "token"
        given(validateUser.validate(userId, token)).willReturn(true)

        // when
        val actual = userController.allowUser(userId, token)

        // then
        assertTrue(actual)
        then(validateUser).should().validate(userId, token)
    }
}
