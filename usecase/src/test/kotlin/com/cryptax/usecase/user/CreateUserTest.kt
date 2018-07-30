package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase create a user")
@ExtendWith(MockitoExtension::class)
class CreateUserTest {

    @Mock
    lateinit var emailService: EmailService
    @Mock
    lateinit var userRepository: UserRepository
    @Mock
    lateinit var securePassword: SecurePassword
    @Mock
    lateinit var idGenerator: IdGenerator
    @InjectMocks
    lateinit var createUser: CreateUser

    private val id = "random id"
    private val hashedPassword = "fqfdwfewfwfwef"
    private val email = "john.doe@protonmail.com"
    private val password = "mypassword".toCharArray()
    private val user = User(id, email, password, "Doe", "John", false)
    private val token = "randomToken"

    @Test
    @DisplayName("Create a user")
    fun testCreate() {
        //given
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.empty())
        given(idGenerator.generate()).willReturn(id)
        given(securePassword.securePassword(password)).willReturn(hashedPassword)
        given(userRepository.create(any())).willReturn(Single.just(user))
        given(securePassword.generateToken(any())).willReturn(token)

        //when
        val actual = createUser.create(user).blockingGet()

        //then
        assertNotNull(actual)
        user.password.forEach { assert(it == '\u0000') }
        then(userRepository).should().findByEmail(user.email)
        then(idGenerator).should().generate()
        // Have to use any() because we reset the password and mockito return the pointer
        then(securePassword).should().securePassword(any())
        argumentCaptor<User>().apply {
            then(securePassword).should().generateToken(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.email).isEqualTo(user.email)
            assertThat(firstValue.password).containsOnly('\u0000')
            assertThat(firstValue.lastName).isEqualTo(user.lastName)
            assertThat(firstValue.firstName).isEqualTo(user.firstName)
            assertThat(firstValue.allowed).isFalse()
        }
        argumentCaptor<User>().apply {
            then(emailService).should().welcomeEmail(capture(), eq(token))
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.email).isEqualTo(user.email)
            assertThat(firstValue.password).containsOnly('\u0000')
            assertThat(firstValue.lastName).isEqualTo(user.lastName)
            assertThat(firstValue.firstName).isEqualTo(user.firstName)
            assertThat(firstValue.allowed).isFalse()
        }
        argumentCaptor<User>().apply {
            then(userRepository).should().create(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.email).isEqualTo(user.email)
            assertThat(firstValue.password.joinToString(separator = "")).isEqualTo(hashedPassword)
            assertThat(firstValue.lastName).isEqualTo(user.lastName)
            assertThat(firstValue.firstName).isEqualTo(user.firstName)
            assertThat(firstValue.allowed).isFalse()
        }
    }

    @Test
    @DisplayName("User already exists")
    fun testCreateAlreadyExists() {
        //given
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))

        //when
        val exception = assertThrows(UserAlreadyExistsException::class.java) {
            createUser.create(user).blockingGet()
        }

        //then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(idGenerator).shouldHaveZeroInteractions()
        then(securePassword).shouldHaveZeroInteractions()
        then(emailService).shouldHaveZeroInteractions()
    }
}
