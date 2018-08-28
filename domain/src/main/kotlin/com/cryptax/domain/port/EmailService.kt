package com.cryptax.domain.port

import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User

interface EmailService {
    fun welcomeEmail(user: User, token: String)

    fun resetPasswordEmail(email: String, resetPassword: ResetPassword)

    fun resetPasswordConfirmationEmail(email: String)
}
