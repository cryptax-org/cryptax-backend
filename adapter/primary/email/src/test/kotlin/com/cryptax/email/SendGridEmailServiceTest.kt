package com.cryptax.email

import com.cryptax.config.EmailProps
import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.charset.Charset
import java.time.ZonedDateTime

@DisplayName("Usescase update a transaction")
@ExtendWith(MockitoExtension::class)
class SendGridEmailServiceTest {

    private val objectMapper = ObjectMapper()
    private val response = Response.Builder()
        .protocol(Protocol.HTTP_1_1)
        .message("dqdwqdq")
        .request(Request.Builder().url("http://google.com").build())
        .code(202)
        .build()
    @Mock
    lateinit var client: OkHttpClient
    @Mock
    lateinit var call: Call

    @Test
    fun `welcome email`() {
        // given
        val user = User(
            id = "userId",
            email = "email@email.com",
            password = "password".toCharArray(),
            lastName = "derp",
            firstName = "derp",
            allowed = false)
        val config = EmailProps(enabled = true, url = "http://baseurl/", function = "sendEmail", from = "from@cryptax.app")
        val emailService = SendGridEmailService(client, config)
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        emailService.welcomeEmail(user, "token")

        // then
        argumentCaptor<Request>().apply {
            then(client).should().newCall(capture())
            val request = firstValue
            assertThat(request.url().toString()).isEqualTo("http://baseurl/sendEmail?sg_key=")
            assertThat(request.method()).isEqualTo("POST")
            val buffer = Buffer()
            request.body()!!.writeTo(buffer)
            val snapshot = buffer.snapshot().string(Charset.forName("UTF-8"))
            val map = objectMapper.readValue(snapshot, Map::class.java)
            assertThat(map["to"]).isEqualTo(user.email)
            assertThat(map["from"]).isEqualTo(config.from)
            assertThat(map["subject"]).isEqualTo("Welcome to Cryptax")
            assertThat(map["body"]).isNotNull
        }
    }

    @Test
    fun `welcome email, not enabled`() {
        // given
        val user = User(
            id = "userId",
            email = "email@email.com",
            password = "password".toCharArray(),
            lastName = "derp",
            firstName = "derp",
            allowed = false)
        val config = EmailProps(enabled = false)
        val emailService = SendGridEmailService(client, config)

        // when
        emailService.welcomeEmail(user, "token")

        // then
        then(client).shouldHaveZeroInteractions()
    }

    @Test
    fun `welcome email failed`() {
        // given
        val user = User(
            id = "userId",
            email = "email@email.com",
            password = "password".toCharArray(),
            lastName = "derp",
            firstName = "derp",
            allowed = false)
        val config = EmailProps(enabled = true, url = "http://baseurl/", function = "sendEmail", from = "from@cryptax.app")
        val emailService = SendGridEmailService(client, config)
        val response = Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .message("dqdwqdq")
            .request(Request.Builder().url("http://google.com").build())
            .code(400)
            .build()
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        emailService.welcomeEmail(user, "token")

        // then
        argumentCaptor<Request>().apply {
            then(client).should().newCall(capture())
            val request = firstValue
            assertThat(request.url().toString()).isEqualTo("http://baseurl/sendEmail?sg_key=")
            assertThat(request.method()).isEqualTo("POST")
            val buffer = Buffer()
            request.body()!!.writeTo(buffer)
            val snapshot = buffer.snapshot().string(Charset.forName("UTF-8"))
            val map = objectMapper.readValue(snapshot, Map::class.java)
            assertThat(map["to"]).isEqualTo(user.email)
            assertThat(map["from"]).isEqualTo(config.from)
            assertThat(map["subject"]).isEqualTo("Welcome to Cryptax")
            assertThat(map["body"]).isNotNull
        }
    }

    @Test
    fun `reset password email`() {
        // given
        val email = "email@email.com"
        val resetPassword = ResetPassword("userId", "token", ZonedDateTime.now())
        val config = EmailProps(enabled = true, url = "http://baseurl/", function = "sendEmail", from = "from@cryptax.app")
        val emailService = SendGridEmailService(client, config)
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        emailService.resetPasswordEmail(email, resetPassword)

        // then
        argumentCaptor<Request>().apply {
            then(client).should().newCall(capture())
            val request = firstValue
            assertThat(request.url().toString()).isEqualTo("http://baseurl/sendEmail?sg_key=")
            assertThat(request.method()).isEqualTo("POST")
            val buffer = Buffer()
            request.body()!!.writeTo(buffer)
            val snapshot = buffer.snapshot().string(Charset.forName("UTF-8"))
            val map = objectMapper.readValue(snapshot, Map::class.java)
            assertThat(map["to"]).isEqualTo(email)
            assertThat(map["from"]).isEqualTo(config.from)
            assertThat(map["subject"]).isEqualTo("Reset password")
            assertThat(map["body"]).isNotNull
        }
    }

    @Test
    fun `reset password email, not enabled`() {
        // given
        val email = "email@email.com"
        val resetPassword = ResetPassword("userId", "token", ZonedDateTime.now())
        val config = EmailProps(enabled = false)
        val emailService = SendGridEmailService(client, config)

        // when
        emailService.resetPasswordEmail(email, resetPassword)

        // then
        then(client).shouldHaveZeroInteractions()
    }

    @Test
    fun `reset password confirmation email`() {
        // given
        val email = "email@email.com"
        val config = EmailProps(enabled = true, url = "http://baseurl/", function = "sendEmail", from = "from@cryptax.app")
        val emailService = SendGridEmailService(client, config)
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        emailService.resetPasswordConfirmationEmail(email)

        // then
        argumentCaptor<Request>().apply {
            then(client).should().newCall(capture())
            val request = firstValue
            assertThat(request.url().toString()).isEqualTo("http://baseurl/sendEmail?sg_key=")
            assertThat(request.method()).isEqualTo("POST")
            val buffer = Buffer()
            request.body()!!.writeTo(buffer)
            val snapshot = buffer.snapshot().string(Charset.forName("UTF-8"))
            val map = objectMapper.readValue(snapshot, Map::class.java)
            assertThat(map["to"]).isEqualTo(email)
            assertThat(map["from"]).isEqualTo(config.from)
            assertThat(map["subject"]).isEqualTo("Your password has been changed")
            assertThat(map["body"]).isNotNull
        }
    }

    @Test
    fun `reset password confirmation email not enabled`() {
        // given
        val email = "email@email.com"
        val config = EmailProps(enabled = false)
        val emailService = SendGridEmailService(client, config)

        // when
        emailService.resetPasswordConfirmationEmail(email)

        // then
        then(client).shouldHaveZeroInteractions()
    }
}
