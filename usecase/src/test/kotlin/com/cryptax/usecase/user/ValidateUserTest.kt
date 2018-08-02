package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.nhaarman.mockitokotlin2.willThrow
import io.reactivex.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase validate users")
@ExtendWith(MockitoExtension::class)
class ValidateUserTest {

    @Mock
    lateinit var securePassword: SecurePassword
    @Mock
    lateinit var userRepository: UserRepository
    @InjectMocks
    lateinit var findUser: ValidateUser

    @Test
    fun testValidate() {
        // given
        val userId = "id"
        val user = User(userId, "", "".toCharArray(), "", "", true)
        val welcomeToken = "token"
        given(userRepository.findById(userId)).willReturn(Maybe.just(user))
        given(securePassword.generateToken(user)).willReturn(welcomeToken)

        // when
        val actual = findUser.validate(userId, welcomeToken).blockingGet()

        // then
        assertThat(actual).isEqualTo(true)
        then(userRepository).should().findById(userId)
        then(userRepository).should().updateUser(user)
        then(securePassword).should().generateToken(user)
    }

    @Test
    fun testValidateNotFound() {
        // given
        val userId = "id"
        val welcomeToken = "token"
        given(userRepository.findById(userId)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows<UserNotFoundException> {
            findUser.validate(userId, welcomeToken).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(userId)
        then(userRepository).should().findById(userId)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(securePassword).shouldHaveZeroInteractions()
    }

    @Test
    fun testValidateError() {
        // given
        val userId = "id"
        val welcomeToken = "token"
        given(userRepository.findById(userId)).willThrow { RuntimeException("Error") }

        // when
        val exception = assertThrows<RuntimeException> {
            findUser.validate(userId, welcomeToken).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo("Error")
        then(userRepository).should().findById(userId)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(securePassword).shouldHaveZeroInteractions()
    }
}
