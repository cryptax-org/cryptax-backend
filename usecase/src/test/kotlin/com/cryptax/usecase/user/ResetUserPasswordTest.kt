package com.cryptax.usecase.user

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.ResetPasswordException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZoneId
import java.time.ZonedDateTime

@DisplayName("Usescase reset password")
@ExtendWith(MockitoExtension::class)
class ResetUserPasswordTest {

    private val user = User("id", "john.doe@proton.com", "password".toCharArray(), "Doe", "John", true)

    @Mock
    lateinit var idGenerator: IdGenerator
    @Mock
    lateinit var emailService: EmailService
    @Mock
    lateinit var securePassword: SecurePassword
    @Mock
    lateinit var userRepository: UserRepository
    @Mock
    lateinit var resetPasswordRepository: ResetPasswordRepository
    @InjectMocks
    lateinit var resetUserPassword: ResetUserPassword

    @Test
    fun `initiate password reset`() {
        // given
        val resetPassword = ResetPassword(user.id, "456", ZonedDateTime.now(ZoneId.of("UTC")))
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(idGenerator.generate()).willReturn("456")
        given(resetPasswordRepository.save(any())).willReturn(Single.just(resetPassword))

        // when
        val actual = resetUserPassword.initiatePasswordReset(user.email).blockingGet()

        // then
        assertThat(actual).isEqualTo(resetPassword)
        then(userRepository).should().findByEmail(user.email)
        then(idGenerator).should().generate()
        argumentCaptor<ResetPassword>().apply {
            then(resetPasswordRepository).should().save(capture())
            assertThat(firstValue.userId).isEqualTo(resetPassword.userId)
            assertThat(firstValue.token).isEqualTo(resetPassword.token)
            assertThat(firstValue.date).isNotNull()
        }
        then(emailService).should().resetPasswordEmail(user.email, resetPassword)
    }

    @Test
    fun `initiate password reset, user not found`() {
        // given
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(UserNotFoundException::class.java) {
            resetUserPassword.initiatePasswordReset(user.email).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).should().findByEmail(user.email)
    }

    @Test
    fun `inititate password reset fails`() {
        // given
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(idGenerator.generate()).willThrow(RuntimeException())

        // when
        assertThrows(RuntimeException::class.java) {
            resetUserPassword.initiatePasswordReset(user.email).blockingGet()
        }

        // then
        then(userRepository).should().findByEmail(user.email)
        then(idGenerator).should().generate()
    }

    @Test
    fun `reset password`() {
        // given
        val password = "derp".toCharArray()
        val hashedPassword = "hashedPassword"
        val token = "mytoken"
        val resetPassword = ResetPassword(user.id, token, ZonedDateTime.now(ZoneId.of("UTC")))
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(resetPasswordRepository.findByUserId(user.id)).willReturn(Maybe.just(resetPassword))
        given(securePassword.securePassword(password)).willReturn(hashedPassword)
        given(userRepository.updateUser(any())).willReturn(Single.just(user))
        given(resetPasswordRepository.delete(user.id)).willReturn(Single.just(Unit))

        // when
        val actual = resetUserPassword.resetPassword(user.email, password, token).blockingGet()

        // then
        assertThat(actual).isEqualTo(Unit)
        then(userRepository).should().findByEmail(user.email)
        then(resetPasswordRepository).should().findByUserId(user.id)
        argumentCaptor<User>().apply {
            then(userRepository).should().updateUser(capture())
            assertThat(firstValue.id).isEqualTo(user.id)
            assertThat(firstValue.email).isEqualTo(user.email)
            assertThat(firstValue.password).isEqualTo(hashedPassword.toCharArray())
            assertThat(firstValue.lastName).isEqualTo(user.lastName)
            assertThat(firstValue.firstName).isEqualTo(user.firstName)
            assertThat(firstValue.allowed).isEqualTo(user.allowed)
        }
        then(emailService).should().resetPasswordConfirmationEmail(user.email)
        then(securePassword).should().securePassword(password)
        then(resetPasswordRepository).should().delete(user.id)
    }

    @Test
    fun `reset password, user not found`() {
        // given
        val password = "derp".toCharArray()
        val token = "mytoken"
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(ResetPasswordException::class.java) {
            resetUserPassword.resetPassword(user.email, password, token).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).should().findByEmail(user.email)
        then(emailService).shouldHaveZeroInteractions()
    }

    @Test
    fun `reset password, reset password not found`() {
        // given
        val password = "derp".toCharArray()
        val token = "mytoken"
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(resetPasswordRepository.findByUserId(user.id)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(ResetPasswordException::class.java) {
            resetUserPassword.resetPassword(user.email, password, token).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).should().findByEmail(user.email)
        then(resetPasswordRepository).should().findByUserId(user.id)
        then(emailService).shouldHaveZeroInteractions()
    }

    @Test
    fun `reset password, not valid 1`() {
        // given
        val password = "derp".toCharArray()
        val token = "mytoken"
        val resetPassword = ResetPassword(user.id, token, ZonedDateTime.now(ZoneId.of("UTC")))
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(resetPasswordRepository.findByUserId(user.id)).willReturn(Maybe.just(resetPassword))

        // when
        val exception = assertThrows(ResetPasswordException::class.java) {
            resetUserPassword.resetPassword(user.email, password, "mytokennotgood").blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).should().findByEmail(user.email)
        then(resetPasswordRepository).should().findByUserId(user.id)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(emailService).shouldHaveZeroInteractions()
    }

    @Test
    fun `reset password, not valid 2`() {
        // given
        val password = "derp".toCharArray()
        val token = "mytoken"
        val resetPassword = ResetPassword(user.id, token, ZonedDateTime.now(ZoneId.of("UTC")).minusDays(5))
        given(userRepository.findByEmail(user.email)).willReturn(Maybe.just(user))
        given(resetPasswordRepository.findByUserId(user.id)).willReturn(Maybe.just(resetPassword))

        // when
        val exception = assertThrows(ResetPasswordException::class.java) {
            resetUserPassword.resetPassword(user.email, password, token).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(user.email)
        then(userRepository).should().findByEmail(user.email)
        then(resetPasswordRepository).should().findByUserId(user.id)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(emailService).shouldHaveZeroInteractions()
    }
}
