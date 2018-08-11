package com.cryptax.config.dto

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

data class PropertiesDto(val server: ServerDto, val jwt: JwtDto, val email: EmailDto, val http: HttpDto, val db: DbDto)
data class ServerDto(val domain: String, val port: Int)
data class JwtDto(
    val keyStorePath: String,
    private var password: String,
    val algorithm: String,
    val issuer: String,
    val expiresInMinutes: Int,
    val refreshExpiresInDays: Int) {

    fun password(profile: String? = null): String {
        if (profile == "it") return password
        return decrypt(password)
    }
}

data class EmailDto(val host: String, val port: Int, val username: String, private var password: String, val from: String) {
    fun password(profile: String? = null): String {
        if (profile == "it") return password
        return decrypt(password)
    }
}

data class HttpDto(val maxIdleConnections: Int, val keepAliveDuration: Long)

data class DbDto(val mode: String, val url: String?, val name: String?, val username: String?, private val password: String?, val socketFactory: String?, val socketFactoryArg: String?, val useSSL: Boolean?) {

    private fun password(): String {
        return decrypt(password!!)
    }

    fun connectionUrl(): String {
        return "$url/$name?useSSL=$useSSL&socketFactoryArg=$socketFactoryArg&socketFactory=$socketFactory&user=$username&password=${password()}"
    }
}

data class GoogleCredentialsDto(
    val type: String,
    val project_id: String,
    val private_key_id: String,
    val private_key: String,
    val client_email: String,
    val client_id: String,
    val auth_uri: String,
    val token_uri: String,
    val auth_provider_x509_cert_url: String,
    val client_x509_cert_url: String)

fun decrypt(password: String): String {
    val stringEncryptor = StandardPBEStringEncryptor()
    val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
    val argument = runtimeMxBean.inputArguments.find { s -> s.contains("jasypt.encryptor.password") } ?: throw RuntimeException("jasypt.encryptor.password not found")
    val jasyptPassword = argument.substring(argument.indexOf("=") + 1)
    stringEncryptor.setPassword(jasyptPassword)
    return stringEncryptor.decrypt(password)
}
