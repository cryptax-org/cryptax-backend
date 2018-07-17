package com.cryptax.email

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService

class VertxEmailClient : EmailService {

    override fun welcomeEmail(user: User, token: String) {
        println("Send email to ${user.email}")
    }
}
