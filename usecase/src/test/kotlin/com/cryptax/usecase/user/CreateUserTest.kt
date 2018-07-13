package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import org.junit.jupiter.api.Assertions.assertEquals
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
    lateinit var userRepository: UserRepository
    @Mock
    lateinit var securePassword: SecurePassword
    @Mock
    lateinit var idGenerator: IdGenerator
    @InjectMocks
    lateinit var createUser: CreateUser

    private val id = "random id"
    private val hashedPassword = "fqfdwfewfwfwef"
    private val email = "john.doe@proton.com"
    private val password = "mypassword".toCharArray()
    private val user = User("1", email, password, "Doe", "John")

    @Test
    @DisplayName("Create a user")
    fun testCreate() {
        //given
        given(userRepository.findByEmail(user.email)).willReturn(null)
        given(idGenerator.generate()).willReturn(id)
        given(securePassword.securePassword(password)).willReturn(hashedPassword)
        given(userRepository.create(any())).willReturn(user)

        //when
        val actual = createUser.create(user)

        //then
        assertNotNull(actual)
        user.password.forEach { assert(it == '\u0000') }
        then(userRepository).should().findByEmail(user.email)
        then(idGenerator).should().generate()
        // Have to use any() because we reset the password and mockito return the pointer
        then(securePassword).should().securePassword(any())
        // TODO see if there a better way to use mockito-kotlin for argument captor
        argumentCaptor<User>().apply {
            then(userRepository).should().create(capture())
            assertEquals(id, firstValue.id)
            assertEquals(user.email, firstValue.email)
            assertEquals(hashedPassword, firstValue.password.joinToString(separator = ""))
            assertEquals(user.lastName, firstValue.lastName)
            assertEquals(user.firstName, firstValue.firstName)
        }
    }

    @Test
    @DisplayName("User already exists")
    fun testCreateAlreadyExists() {
        //given
        given(userRepository.findByEmail(user.email)).willReturn(user)

        //when
        val exception = assertThrows(UserAlreadyExistsException::class.java) {
            createUser.create(user)
        }

        //then
        assertEquals(user.email, exception.message)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(idGenerator).shouldHaveZeroInteractions()
        then(securePassword).shouldHaveZeroInteractions()
    }
}
