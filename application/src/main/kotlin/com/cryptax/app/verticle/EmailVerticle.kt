package com.cryptax.app.verticle

import com.cryptax.config.AppConfig
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.ext.mail.MailMessage
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.ext.mail.MailClient
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

private val log: Logger = LoggerFactory.getLogger(EmailVerticle::class.java)

class EmailVerticle(private val appConfig: AppConfig, kodein: Kodein) : AbstractVerticle() {

    private val mailClient by kodein.instance<MailClient>()

    // TODO: To refactor
    override fun start() {
        val eb = vertx.eventBus()
        val welcomeEmailConsumer = eb.consumer<JsonObject>("cryptax.email.welcome")
        welcomeEmailConsumer
            .toFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { message: Message<JsonObject> ->
                val email = message.body().getString("to")
                val mailMessage = MailMessage(
                    from = appConfig.properties.email.from,
                    to = listOf(email),
                    subject = message.body().getString("subject"),
                    html = message.body().getString("html"))
                mailMessage
            }
            .flatMap { mailMessage ->
                log.info("Sending welcome email to ${mailMessage.to}")
                mailClient.rxSendMail(mailMessage).toFlowable()
            }
            .subscribe(
                { mailResult -> log.info("Welcome email sent to ${mailResult.recipients} with message id [${mailResult.messageID}]") },
                { t: Throwable -> log.error("Welcome email not sent", t) })

        val resetPasswordEmailConsumer = eb.consumer<JsonObject>("cryptax.email.reset.password")
        resetPasswordEmailConsumer
            .toFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { message: Message<JsonObject> ->
                val email = message.body().getString("to")
                val mailMessage = MailMessage(
                    from = appConfig.properties.email.from,
                    to = listOf(email),
                    subject = message.body().getString("subject"),
                    html = message.body().getString("html"))
                mailMessage
            }
            .flatMap { mailMessage ->
                log.info("Sending reset password email to ${mailMessage.to}")
                mailClient.rxSendMail(mailMessage).toFlowable()
            }
            .subscribe(
                { mailResult -> log.info("Reset password email sent to ${mailResult.recipients} with message id [${mailResult.messageID}]") },
                { t: Throwable -> log.error("Reset password email not sent", t) })

        val changedPasswordEmailConsumer = eb.consumer<JsonObject>("cryptax.email.updated")
        changedPasswordEmailConsumer
            .toFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { message: Message<JsonObject> ->
                val email = message.body().getString("to")
                val mailMessage = MailMessage(
                    from = appConfig.properties.email.from,
                    to = listOf(email),
                    subject = message.body().getString("subject"),
                    html = message.body().getString("html"))
                mailMessage
            }
            .flatMap { mailMessage ->
                log.info("Sending changed password email to ${mailMessage.to}")
                mailClient.rxSendMail(mailMessage).toFlowable()
            }
            .subscribe(
                { mailResult -> log.info("Changed password email sent to ${mailResult.recipients} with message id [${mailResult.messageID}]") },
                { t: Throwable -> log.error("Changed password email not sent", t) })
    }
}
