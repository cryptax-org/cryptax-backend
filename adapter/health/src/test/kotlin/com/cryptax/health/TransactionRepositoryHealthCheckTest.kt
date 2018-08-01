package com.cryptax.health

import com.cryptax.domain.port.TransactionRepository
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
class TransactionRepositoryHealthCheckTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository
    @InjectMocks
    lateinit var healthCheck: TransactionRepositoryHealthCheck

    @Test
    fun testCheck() {
        // given
        given(transactionRepository.ping()).willReturn(true)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isTrue()
        then(transactionRepository).should().ping()
    }

    @Test
    fun testCheckFailed() {
        // given
        given(transactionRepository.ping()).willReturn(false)

        // when
        val actual = healthCheck.execute()

        // then
        assertThat(actual.isHealthy).isFalse()
        assertThat(actual.message).isEqualTo("Can't ping transaction repository")
        then(transactionRepository).should().ping()
    }
}
