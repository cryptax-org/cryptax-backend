package com.cryptax.health

import com.cryptax.domain.port.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Health check transaction repo test")
@ExtendWith(MockitoExtension::class)
class UserRepositoryHealthCheckTest {

    @Mock
    lateinit var userRepository: UserRepository
    @InjectMocks
    lateinit var healthCheck: UserRepositoryHealthCheck

    @Test
    fun `check health`() {
        // given
        given(userRepository.ping()).willReturn(true)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isTrue()
        then(userRepository).should().ping()
    }

    @Test
    fun `check health fails`() {
        // given
        given(userRepository.ping()).willReturn(false)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isFalse()
        assertThat(actual.message).isEqualTo("Can't ping user repository")
        then(userRepository).should().ping()
    }
}
