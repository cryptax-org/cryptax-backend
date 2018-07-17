package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase find users")
@ExtendWith(MockitoExtension::class)
class FindUserTest {

    @Mock
    lateinit var userRepository: UserRepository
    @InjectMocks
    lateinit var findUser: FindUser

    private val id = "1"
    private val user = User(id, "john.doe@proton.com", "mypassword".toCharArray(), "Doe", "John", true)

    @Test
    @DisplayName("Find a user by id")
    fun testFindById() {
        //given
        given(userRepository.findById(id)).willReturn(user)

        //when
        val actual = findUser.findById(id)

        //then
        assertEquals(user, actual)
        then(userRepository).should().findById(id)
    }

    @Test
    @DisplayName("Find a user by id not found")
    fun testFindByIdNotFound() {
        //given
        given(userRepository.findById(id)).willReturn(null)

        //when
        val actual = findUser.findById(id)

        //then
        assertNull(actual)
        then(userRepository).should().findById(id)
    }
}
