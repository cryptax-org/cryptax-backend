package com.cryptax.email

import com.cryptax.config.EmailProps
import com.cryptax.domain.entity.ResetPassword
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SendGridEmailService(
    private val client: OkHttpClient,
    private val config: EmailProps) : EmailService {

    override fun welcomeEmail(user: User, token: String) {
        if (config.enabled) {
            log.info("Send welcome email to ${user.email}")
            sendEmail(user.email, "Welcome to Cryptax", "You can click <a href=\\\"${config.verifyUrl}/${user.id}/$token\\\">here</a> to activate your account")
        }
    }

    override fun resetPasswordEmail(email: String, resetPassword: ResetPassword) {
        if (config.enabled) {
            log.info("Send reset password email to $email")
            sendEmail(email,
                "Reset password",
                "Here is your reset password token: ${resetPassword.token}.<br />You can click <a href=\\\"\\\">here</a> to reset your password")
        }
    }

    override fun resetPasswordConfirmationEmail(email: String) {
        if (config.enabled) {
            log.info("Send password updated email to $email")
            sendEmail(email,
                "Your password has been changed",
                "Your password has been changed.<br />Please contact us if you did not do it blablablabla..")
        }
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val content = createJsonContent(to, subject, body)
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content)
        val request = Request.Builder()
            .url(config.fullUrl)
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        if (response.code() != 202) {
            log.error("Something went wrong when sending the email $to $subject ${response.body()}")
        }
    }

    private fun createJsonContent(to: String, subject: String, body: String): String {
        return "{\"to\":\"$to\",\"from\":\"${config.from}\",\"subject\":\"$subject\",\"body\":\"$body\"}"
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SendGridEmailService::class.java)
    }
}
