package com.cryptax.config.dto

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

data class PropertiesDto(val server: ServerDto, val jwt: JwtDto)
data class ServerDto(val domain: String, val port: Int)
data class JwtDto(
    val keyStorePath: String,
    private var password: String,
    val algorithm: String,
    val issuer: String,
    val expiresInMinutes: Int,
    val refreshExpiresInDays: Int) {

    fun password(profile: String): String {
        if (profile == "it") return password
        return decryptPassword(password)
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
