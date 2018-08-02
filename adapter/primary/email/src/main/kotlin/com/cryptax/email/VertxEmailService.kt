package com.cryptax.email

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(VertxEmailService::class.java)

class VertxEmailService(private val vertx: Vertx) : EmailService {

    private val address = "cryptax.email"

    override fun welcomeEmail(user: User, token: String) {
        val message = JsonObject()
            .put("subject", "Welcome to Cryptax")
            .put("to", user.email)
            .put("html", "Here is your token: $token.<br />You can click <a href=\"http://localhost:8080/users/${user.id}/allow?token=$token\">here</a> to activate your account")

        log.debug("Publishing message on event bus to [$address]")
        vertx.eventBus().publish(address, message)
    }
}
