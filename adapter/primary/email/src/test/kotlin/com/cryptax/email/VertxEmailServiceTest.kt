package com.cryptax.email

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.ZonedDateTime

@ExtendWith(VertxExtension::class)
class VertxEmailServiceTest {

    @Test
    fun testWelcomeEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val user = User(
            id = "userId",
            email = "email@email.com",
            password = "password".toCharArray(),
            lastName = "derp",
            firstName = "derp",
            allowed = false)
        val emailService = VertxEmailService(vertx)
        vertx.eventBus().consumer<JsonObject>("cryptax.email.welcome") { message ->
            // then
            val actual = message.body()
            assertThat(actual).isNotNull
            assertThat(actual.getString("subject")).isEqualTo("Welcome to Cryptax")
            assertThat(actual.getString("to")).isEqualTo("email@email.com")
            assertThat(actual.getString("html")).isNotNull()
            testContext.completeNow()
        }

        // when
        emailService.welcomeEmail(user, "token")
    }

    @Test
    fun testResetPasswordEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val email = "email@email.com"
        val resetPassword = ResetPassword(
            userId = "userId",
            token = "token",
            date = ZonedDateTime.now())
        val emailService = VertxEmailService(vertx)
        vertx.eventBus().consumer<JsonObject>("cryptax.email.reset.password") { message ->
            // then
            val actual = message.body()
            assertThat(actual).isNotNull
            assertThat(actual.getString("subject")).isEqualTo("Reset password")
            assertThat(actual.getString("to")).isEqualTo(email)
            assertThat(actual.getString("html")).isNotNull()
            testContext.completeNow()
        }

        // when
        emailService.resetPasswordEmail(email, resetPassword)
    }

    @Test
    fun testResetPasswordConfirmationEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val email = "email@email.com"
        val emailService = VertxEmailService(vertx)
        vertx.eventBus().consumer<JsonObject>("cryptax.email.updated") { message ->
            // then
            val actual = message.body()
            assertThat(actual).isNotNull
            assertThat(actual.getString("subject")).isEqualTo("Your password has been changed")
            assertThat(actual.getString("to")).isEqualTo(email)
            assertThat(actual.getString("html")).isNotNull()
            testContext.completeNow()
        }

        // when
        emailService.resetPasswordConfirmationEmail(email)
    }
}
