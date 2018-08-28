package com.cryptax.db

import com.cryptax.domain.entity.ResetPassword
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("In memory transaction reset password test")
class InMemoryResetPasswordRepositoryTest {

    private lateinit var repository: InMemoryResetPasswordRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryResetPasswordRepository()
    }

    @Test
    fun testSave() {
        // given
        val resetPassword = ResetPassword("id", "token", ZonedDateTime.now())

        // when
        val actual = repository.save(resetPassword).blockingGet()

        // then
        assertThat(actual).isEqualTo(resetPassword)
    }

    @Test
    fun testFindByUserId() {
        // given
        val resetPassword = ResetPassword("id", "token", ZonedDateTime.now())
        repository.save(resetPassword).blockingGet()

        // when
        val actual = repository.findByUserId(resetPassword.userId).blockingGet()

        // then
        assertThat(actual).isEqualTo(resetPassword)
    }

    @Test
    fun testDelete() {
        // given
        val resetPassword = ResetPassword("id", "token", ZonedDateTime.now())
        repository.save(resetPassword).blockingGet()

        // when
        val actual = repository.delete(resetPassword.userId).blockingGet()

        // then
        assertThat(actual).isEqualTo(Unit)
        val find = repository.findByUserId(resetPassword.userId).blockingGet()
        assertThat(find).isNull()
    }
}
