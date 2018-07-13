package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
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
    lateinit var securePassword: SecurePassword
    @InjectMocks
    lateinit var loginUser: LoginUser

    private val id = "1"
    private val email = "john.doe@proton.com"
    private val password = "mypassword".toCharArray()
    private val hashedPassword = "hashedPassword"
    private val user = User(id, "john.doe@proton.com", hashedPassword.toCharArray(), "Doe", "John")

    @Test
    @DisplayName("Login a user")
    fun testLogin() {
        //given
        given(userRepository.findByEmail(email)).willReturn(user)
        given(securePassword.matchPassword(password, user.password)).willReturn(true)

        //when
        val actual = loginUser.login(email, password)

        //then
        assertEquals(user, actual)
        then(userRepository).should().findByEmail(email)
        then(securePassword).should().matchPassword(password, user.password)
    }

    @Test
    @DisplayName("Login a user not found")
    fun testLoginUserNotFound() {
        //given
        given(userRepository.findByEmail(email)).willReturn(null)

        //when
        val exception = assertThrows(LoginException::class.java) {
            loginUser.login(email, password)
        }

        //then
        assertEquals(email, exception.email)
        assertEquals("User not found", exception.description)
        then(userRepository).should().findByEmail(email)
    }

    @Test
    @DisplayName("Login a user wrong password")
    fun testLoginWrongPassword() {
        //given
        given(userRepository.findByEmail(email)).willReturn(user)
        given(securePassword.matchPassword("wrong password".toCharArray(), user.password)).willReturn(false)

        //when
        val exception = assertThrows(LoginException::class.java) {
            loginUser.login(email, "wrong password".toCharArray())
        }

        //then
        assertEquals(email, exception.email)
        assertEquals("Password do not match", exception.description)
        then(userRepository).should().findByEmail(email)
        then(securePassword).should().matchPassword("wrong password".toCharArray(), user.password)
    }
}
