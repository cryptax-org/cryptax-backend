package com.cryptax.email

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.StartTLSOptions

private val log: Logger = LoggerFactory.getLogger(VertxEmailService::class.java)

class VertxEmailService(vertx: Vertx) : EmailService {

    private val mailClient: MailClient

    init {
        // TODO avoid creating a new vertx instance here
        val config = MailConfig()
        config.hostname = EmailConfig.emailProperties.server.host
        config.port = EmailConfig.emailProperties.server.port
        config.starttls = StartTLSOptions.REQUIRED
        config.username = EmailConfig.emailProperties.email.username
        config.password = EmailConfig.emailProperties.email.password
        config.isTrustAll = true
        config.isSsl = true
        mailClient = MailClient.createShared(vertx, config, "CRYPTAX_POOL")
    }

    override fun welcomeEmail(user: User, token: String) {
        log.info("Sending welcome email to [${user.email}]")

        val message = MailMessage()
        message.from = EmailConfig.emailProperties.email.from
        message.subject = "Welcome to Cryptax"
        message.to = listOf(user.email)
        message.html = "Here is your token: $token.<br />You can click <a href=\"http://localhost:8080/users/${user.id}/allow?token=$token\">here</a> to activate your account"

        mailClient.sendMail(message) { result ->
            if (result.succeeded()) {
                log.info("Welcome email sent to [${user.email}]")
            } else {
                log.error("Failed at sending the welcoming email to [${user.email}]", result.cause())
            }
        }
    }
}
