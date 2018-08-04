package com.cryptax.config

import com.cryptax.config.dto.PropertiesDto
import com.cryptax.config.kodein.KodeinConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions
import io.vertx.kotlin.ext.mail.MailConfig
import org.kodein.di.Kodein

abstract class AppConfig(private val profile: String = "dev", externalKodeinModule: Kodein.Module?) {

    val properties: PropertiesDto = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue(AppConfig::class.java.classLoader.getResourceAsStream("config-${getProfile()}.yml"), PropertiesDto::class.java)

    val kodeinDefaultModule = KodeinConfig(properties, externalKodeinModule).kodeinModule

    val jwtAuthOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = properties.jwt.keyStorePath, password = properties.jwt.password(profile)))
    val jwtOptions = JWTOptions(algorithm = properties.jwt.algorithm, issuer = properties.jwt.issuer, expiresInMinutes = properties.jwt.expiresInMinutes)
    val jwtRefreshOptions = JWTOptions(algorithm = properties.jwt.algorithm, issuer = properties.jwt.issuer, expiresInMinutes = properties.jwt.refreshExpiresInDays)

    val mailConfig = MailConfig(
        hostname = properties.email.host,
        port = properties.email.port,
        starttls = StartTLSOptions.REQUIRED,
        username = properties.email.username,
        password = properties.email.password(profile),
        trustAll = true,
        ssl = true)

    fun getProfile(): String {
        val profileEnv = System.getenv("PROFILE")
        return profileEnv ?: return profile
    }
}

class DefaultAppConfig(profile: String, kodeinModule: Kodein.Module?) : AppConfig(profile = profile, externalKodeinModule = kodeinModule)
