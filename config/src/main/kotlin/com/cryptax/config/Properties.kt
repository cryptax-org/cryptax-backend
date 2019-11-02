package com.cryptax.config

import com.cryptax.config.ProfileType.test
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

data class AppProps(val server: ServerProps, val jwt: JwtProps, val email: EmailProps, val http: HttpProps, val db: DbDProps)
data class ServerProps(val domain: String, val port: Int, val allowOrigin: String)
data class JwtProps(
    val keyStorePath: String,
    private var password: String,
    val algorithm: String,
    val issuer: String,
    val expiresInMinutes: Int,
    val refreshExpiresInDays: Int) {

    fun password(profile: String? = null): String {
        if (profile == test) return password
        return decrypt(password)
    }
}

data class EmailProps(
    val enabled: Boolean,
    val url: String? = null,
    val function: String? = null,
    private val key: String? = null,
    val from: String? = null,
    val verifyUrl: String? = null) {

    val fullUrl = "$url$function?sg_key=" + if (key == null) "" else key()

    private fun key(): String {
        return decrypt(key!!)
    }
}

data class HttpProps(val maxIdleConnections: Int, val keepAliveDuration: Long)

data class DbDProps(val mode: String, val projectId: String?, private val credentials: String?) {
    fun credentials(): String {
        return decrypt(credentials!!)
    }
}

private fun decrypt(password: String): String {
    val stringEncryptor = StandardPBEStringEncryptor()
    val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
    val argument = runtimeMxBean.inputArguments.find { s -> s.contains("jasypt.encryptor.password") } ?: throw RuntimeException("jasypt.encryptor.password not found")
    val jasyptPassword = argument.substring(argument.indexOf("=") + 1)
    stringEncryptor.setPassword(jasyptPassword)
    return stringEncryptor.decrypt(password)
}
