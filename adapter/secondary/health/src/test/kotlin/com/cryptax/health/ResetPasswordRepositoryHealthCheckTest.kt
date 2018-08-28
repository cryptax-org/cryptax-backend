package com.cryptax.health

import com.cryptax.domain.port.ResetPasswordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Health check reset password test")
@ExtendWith(MockitoExtension::class)
class ResetPasswordRepositoryHealthCheckTest {

    @Mock
    lateinit var resetPasswordRepository: ResetPasswordRepository
    @InjectMocks
    lateinit var healthCheck: ResetPasswordRepositoryHealthCheck

    @Test
    fun testCheck() {
        // given
        given(resetPasswordRepository.ping()).willReturn(true)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isTrue()
        then(resetPasswordRepository).should().ping()
    }

    @Test
    fun testCheckFailed() {
        // given
        given(resetPasswordRepository.ping()).willReturn(false)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isFalse()
        assertThat(actual.message).isEqualTo("Can't ping transaction repository")
        then(resetPasswordRepository).should().ping()
    }

}
