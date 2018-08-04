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

    override fun start() {
        val eb = vertx.eventBus()
        val emailConsumer = eb.consumer<JsonObject>("cryptax.email")
        emailConsumer
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
    }
}
