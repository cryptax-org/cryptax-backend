package com.cryptax.email

import com.cryptax.domain.entity.User
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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
        vertx.eventBus().consumer<JsonObject>("cryptax.email") { message ->
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
}
