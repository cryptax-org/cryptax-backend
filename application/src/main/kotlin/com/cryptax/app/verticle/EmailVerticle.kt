package com.cryptax.app.verticle

import com.cryptax.email.EmailConfig
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.ext.mail.MailMessage
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.ext.mail.MailClient


private val log: Logger = LoggerFactory.getLogger(EmailVerticle::class.java)

class EmailVerticle : AbstractVerticle() {

    private val config: MailConfig = MailConfig()

    init {
        config.hostname = EmailConfig.emailProperties.server.host
        config.port = EmailConfig.emailProperties.server.port
        config.starttls = StartTLSOptions.REQUIRED
        config.username = EmailConfig.emailProperties.email.username
        config.password = EmailConfig.emailProperties.email.password
        config.isTrustAll = true
        config.isSsl = true
    }

    override fun start() {
        val mailClient = MailClient.createShared(vertx, config, "CRYPTAX_POOL")

        val eb = vertx.eventBus()
        val emailConsumer = eb.consumer<JsonObject>("cryptax.email")
        emailConsumer
            .toFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { message: Message<JsonObject> ->
                val email = message.body().getString("to")
                val mailMessage = MailMessage(
                    from = EmailConfig.emailProperties.email.from,
                    to = listOf(email),
                    subject = message.body().getString("subject"),
                    html = message.body().getString("html"))
                log.info("Sending welcome email to [$email]")
                mailMessage
            }
            .flatMap { mailMessage ->
                log.info("Sending welcome email 2")
                mailClient.rxSendMail(mailMessage).toFlowable()
            }
            .subscribe(
                { mailResult -> log.info("Welcome email sent to ${mailResult.recipients} with message id [${mailResult.messageID}]") },
                { t: Throwable -> log.error("Welcome email not sent", t) })
    }
}
