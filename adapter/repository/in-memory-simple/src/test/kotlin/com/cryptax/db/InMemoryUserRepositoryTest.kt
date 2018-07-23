package com.cryptax.db

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("In memory user repository test")
class InMemoryUserRepositoryTest {

    private lateinit var userRepository: UserRepository
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
    }

    @BeforeAll
    internal fun beforeAll() {
        user = objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
    }

    @DisplayName("Create user")
    @Test
    fun testCreate() {
        // when
        val actual = userRepository.create(user).blockingGet()

        // then
        assertEquals(user, actual)
    }

    @DisplayName("Create user with null user id")
    @Test
    fun testCreateNullUserId() {
        // given
        val user = objectMapper.readValue(this::class.java.getResourceAsStream("/User2.json"), User::class.java)

        // when
        assertThrows(NullPointerException::class.java) {
            userRepository.create(user).blockingGet()
        }

        // then
        // exception
    }

    @DisplayName("Find by id")
    @Test
    fun testFindById() {
        // given
        userRepository.create(user).blockingGet()

        // when
        val actual = userRepository.findById(user.id!!).blockingGet()

        // then
        assertEquals(user, actual)
    }

    @DisplayName("Find by id not found")
    @Test
    fun testFindByIdNotFound() {
        // given
        val userId = ""

        // when
        val actual = userRepository.findById(userId).blockingGet()

        // then
        assertNull(actual)
    }

    @DisplayName("Find by email")
    @Test
    fun testFindByEmail() {
        // given
        userRepository.create(user).blockingGet()

        // when
        val actual = userRepository.findByEmail(user.email).blockingGet()

        // then
        assertEquals(user, actual)
    }

    @DisplayName("Find by email not found")
    @Test
    fun testFindByEmailNotFound() {
        // given
        val email = ""

        // when
        val actual = userRepository.findByEmail(email).blockingGet()

        // then
        assertNull(actual)
    }
}
