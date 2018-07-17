package com.cryptax.email

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import io.vertx.core.Vertx
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.StartTLSOptions

class VertxEmailClient : EmailService {

    private val mailClient: MailClient

    init {
        val vertx = Vertx.vertx()
        val config = MailConfig()
        config.hostname = EmailConfig.emailProperties.server.host
        config.port = EmailConfig.emailProperties.server.port!!
        config.starttls = StartTLSOptions.REQUIRED
        config.username = EmailConfig.emailProperties.email.username
        config.password = EmailConfig.emailProperties.email.password
        config.isTrustAll = true
        config.isSsl = true
        mailClient = MailClient.createShared(vertx, config, "cryptaxpool")
    }

    override fun welcomeEmail(user: User, token: String) {
        println("Send email to ${user.email}")

        val message = MailMessage()
        message.from = EmailConfig.emailProperties.email.from
        message.to = listOf(user.email)
        message.html = "Here is your token: $token. <br />You can click <a href=\"http://localhost:8080/users/${user.id}/allow?token=$token\">here</a> to activate your account"

        mailClient.sendMail(message) { result ->
            if (result.succeeded()) {
                println(result.result())
            } else {
                println("Failed " + result.cause())
            }
        }
    }
}
