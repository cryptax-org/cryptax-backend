package com.cryptax.config.gcp

import com.cryptax.config.AppConfig
import com.cryptax.config.dto.decrypt
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.sqladmin.SQLAdminScopes
import com.google.cloud.sql.CredentialFactory
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

object GcpConfig {
    fun googleCredentials(): String {
        return decrypt(IOUtils.toString(AppConfig::class.java.classLoader.getResourceAsStream("Cryptax-credentials-enc.json"), Charset.forName("UTF-8")))
    }
}

/**
 * Replace default google credential factory by our own so we can store the json credentials on github
 */
class CryptaxCredentialFactory : CredentialFactory {
    override fun create(): Credential {
        val credential = GoogleCredential.fromStream(GcpConfig.googleCredentials().byteInputStream())
        return credential.createScoped(listOf(SQLAdminScopes.SQLSERVICE_ADMIN))
    }
}
