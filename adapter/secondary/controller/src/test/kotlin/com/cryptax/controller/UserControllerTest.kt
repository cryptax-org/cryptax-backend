package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ValidateUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun testCreateUser() {
        // given
        given(createUser.create(any())).willReturn(Single.just(Pair(user, welcomeToken)))

        // when
        val actual = userController.createUser(userWeb).blockingGet()

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
            assertThat(firstValue.id).isEqualTo("DEFAULT")
            assertThat(userWeb.email).isEqualTo(firstValue.email)
            assertThat(password).isEqualTo(firstValue.password)
            assertThat(userWeb.lastName).isEqualTo(firstValue.lastName)
            assertThat(userWeb.firstName).isEqualTo(firstValue.firstName)
        }
    }

    @Test
    fun testLoginUser() {
        // given
        val email = "email@email.com"
        val password = "mypassword".toCharArray()
        given(loginUser.login(email, password)).willReturn(Single.just(user))

        // when
        val actual = userController.login(email, password).blockingGet()

        // then
        assertThat(user.id).isEqualTo(actual.id)
        assertThat(user.email).isEqualTo(actual.email)
        assertThat(user.lastName).isEqualTo(actual.lastName)
        assertThat(user.firstName).isEqualTo(actual.firstName)
        then(loginUser).should().login(email, password)
    }

    @Test
    fun testFindUser() {
        // given
        val userId = "random user id"
        given(findUser.findById(userId)).willReturn(Maybe.just(user))

        // when
        val actual = userController.findUser(userId).blockingGet()

        // then
        assertThat(actual).isNotNull
        assertThat(user.id).isEqualTo(actual!!.id)
        assertThat(user.email).isEqualTo(actual.email)
        assertThat(user.lastName).isEqualTo(actual.lastName)
        assertThat(user.firstName).isEqualTo(actual.firstName)
        then(findUser).should().findById(userId)
    }

    @Test
    fun testFindUserNotFound() {
        // given
        val userId = "random user id"
        given(findUser.findById(userId)).willReturn(Maybe.empty())

        // when
        val actual = userController.findUser(userId).blockingGet()

        // then
        assertThat(actual).isNull()
        then(findUser).should().findById(userId)
    }

    @Test
    fun testAllowUser() {
        // given
        val userId = "userId"
        val token = "token"
        given(validateUser.validate(userId, token)).willReturn(Single.just(true))

        // when
        val actual = userController.allowUser(userId, token).blockingGet()

        // then
        assertThat(actual).isTrue()
        then(validateUser).should().validate(userId, token)
    }
}
