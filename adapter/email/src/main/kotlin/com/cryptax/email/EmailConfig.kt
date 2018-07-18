package com.cryptax.email

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

object EmailConfig {

    val emailProperties: EmailProperties

    init {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        emailProperties = mapper.readValue(EmailConfig::class.java.classLoader.getResourceAsStream("email.yml"), EmailProperties::class.java)
        val decryptedPassword = decryptPassword(emailProperties.email.password)
        emailProperties.email.password = decryptedPassword
    }

    private fun decryptPassword(password: String): String {
        val stringEncryptor = StandardPBEStringEncryptor()
        val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
        val argument = runtimeMxBean.inputArguments.find { s -> s.contains("jasypt.encryptor.password") } ?: throw RuntimeException("jasypt.encryptor.password not found")
        val jasyptPassword = argument.substring(argument.indexOf("=") + 1)
        stringEncryptor.setPassword(jasyptPassword)
        return stringEncryptor.decrypt(password)
    }
}

data class EmailProperties(val server: Server, val email: Email) {
    data class Server(val host: String, val port: Int)
    data class Email(val username: String, var password: String, var from: String)
}

