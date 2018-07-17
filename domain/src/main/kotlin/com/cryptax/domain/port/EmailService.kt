package com.cryptax.domain.port

import com.cryptax.domain.entity.User

interface EmailService {
    fun welcomeEmail(user: User, token: String)
}
