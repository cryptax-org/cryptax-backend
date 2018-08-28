package com.cryptax.app.verticle

import com.cryptax.app.config.TestAppConfig
import com.nhaarman.mockitokotlin2.any
import io.reactivex.Single
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.MailResult
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.reactivex.ext.mail.MailClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class EmailVerticleTest {

    lateinit var mailClient: MailClient

    @BeforeEach
    fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
        val config = TestAppConfig()
        mailClient = mock(MailClient::class.java)
        val kodin = Kodein { bind<MailClient>() with singleton { mailClient } }
        val emailVerticle = EmailVerticle(config, kodin)
        vertx.deployVerticle(emailVerticle) { ar ->
            if (ar.succeeded())
                testContext.completeNow()
            else
                testContext.failNow(AssertionError("Something went wrong"))
        }
        testContext.awaitCompletion(5, TimeUnit.SECONDS)
    }

    @Test
    fun testWelcomeEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val message = JsonObject()
            .put("subject", "Welcome to Cryptax")
            .put("to", "email@email.com")
            .put("html", "Here is your token: 12345")
        given(mailClient.rxSendMail(any())).will { invocation ->
            val mailMessage: MailMessage = invocation.getArgument<MailMessage>(0)
            // then
            assertThat(mailMessage.from).isEqualTo("webmaster.stock@yahoo.com")
            assertThat(mailMessage.to).isEqualTo(listOf("email@email.com"))
            assertThat(mailMessage.subject).isEqualTo("Welcome to Cryptax")
            assertThat(mailMessage.html).isEqualTo("Here is your token: 12345")
            testContext.completeNow()
            Single.just(MailResult())
        }

        // when
        vertx.eventBus().publish("cryptax.email.welcome", message)
    }

    @Test
    fun testResetPasswordEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val message = JsonObject()
            .put("subject", "Welcome to Cryptax")
            .put("to", "email@email.com")
            .put("html", "Here is your token: 12345")
        given(mailClient.rxSendMail(any())).will { invocation ->
            val mailMessage: MailMessage = invocation.getArgument<MailMessage>(0)
            // then
            assertThat(mailMessage.from).isEqualTo("webmaster.stock@yahoo.com")
            assertThat(mailMessage.to).isEqualTo(listOf("email@email.com"))
            assertThat(mailMessage.subject).isEqualTo("Welcome to Cryptax")
            assertThat(mailMessage.html).isEqualTo("Here is your token: 12345")
            testContext.completeNow()
            Single.just(MailResult())
        }

        // when
        vertx.eventBus().publish("cryptax.email.reset.password", message)
    }

    @Test
    fun testChangePasswordEmail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val message = JsonObject()
            .put("subject", "Welcome to Cryptax")
            .put("to", "email@email.com")
            .put("html", "Here is your token: 12345")
        given(mailClient.rxSendMail(any())).will { invocation ->
            val mailMessage: MailMessage = invocation.getArgument<MailMessage>(0)
            // then
            assertThat(mailMessage.from).isEqualTo("webmaster.stock@yahoo.com")
            assertThat(mailMessage.to).isEqualTo(listOf("email@email.com"))
            assertThat(mailMessage.subject).isEqualTo("Welcome to Cryptax")
            assertThat(mailMessage.html).isEqualTo("Here is your token: 12345")
            testContext.completeNow()
            Single.just(MailResult())
        }

        // when
        vertx.eventBus().publish("cryptax.email.updated", message)
    }
}
