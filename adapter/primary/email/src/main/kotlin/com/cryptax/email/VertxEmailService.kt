package com.cryptax.email

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(VertxEmailService::class.java)

class VertxEmailService(private val vertx: Vertx) : EmailService {

    private val welcomeEmail = "cryptax.email.welcome"
    private val resetPasswordEmail = "cryptax.email.reset.password"
    private val changedPasswordEmail = "cryptax.email.updated"

    override fun welcomeEmail(user: User, token: String) {
        val message = JsonObject()
            .put("subject", "Welcome to Cryptax")
            .put("to", user.email)
            .put("html", "Here is your token: $token.<br />You can click <a href=\"http://localhost:8080/users/${user.id}/allow?token=$token\">here</a> to activate your account")
        publishMessage(welcomeEmail, message)
    }

    override fun resetPasswordEmail(email: String, resetPassword: ResetPassword) {
        val message = JsonObject()
            .put("subject", "Reset password")
            .put("to", email)
            .put("html", "Here is your reset password token: ${resetPassword.token}.<br />You can click <a href=\"\">here</a> to reset your password")
        publishMessage(resetPasswordEmail, message)
    }

    override fun resetPasswordConfirmationEmail(email: String) {
        val message = JsonObject()
            .put("subject", "Your password has been changed")
            .put("to", email)
            .put("html", "Your password has been changed.<br />Please contact us if you did not do it blablablabla..")
        publishMessage(changedPasswordEmail, message)
    }

    private fun publishMessage(destination: String, message: JsonObject) {
        log.debug("Publishing message on event bus to [$destination]")
        vertx.eventBus().publish(destination, message)
    }
}
