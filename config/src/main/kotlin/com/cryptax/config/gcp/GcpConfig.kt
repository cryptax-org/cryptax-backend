package com.cryptax.config.gcp

import com.cryptax.config.AppConfig
import com.cryptax.config.dto.GoogleCredentialsDto
import com.cryptax.config.dto.decryptPassword
import com.cryptax.config.jackson.JacksonConfig
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.sqladmin.SQLAdminScopes

private object GcpConfig {

    fun googleCredentials(): String {
        val googleCredentials = JacksonConfig.objectMapper
            .readValue(AppConfig::class.java.classLoader.getResourceAsStream("Cryptax-credentials.json"), GoogleCredentialsDto::class.java)
        val googleCredentialsWithPrivateKey = GoogleCredentialsDto(
            type = googleCredentials.type,
            project_id = googleCredentials.project_id,
            private_key_id = googleCredentials.private_key_id,
            private_key = decryptPassword(googleCredentials.private_key),
            client_email = googleCredentials.client_email,
            client_id = googleCredentials.client_id,
            auth_uri = googleCredentials.auth_uri,
            token_uri = googleCredentials.token_uri,
            auth_provider_x509_cert_url = googleCredentials.auth_provider_x509_cert_url,
            client_x509_cert_url = googleCredentials.client_x509_cert_url)
        return JacksonConfig.objectMapper.writeValueAsString(googleCredentialsWithPrivateKey).replace("\\\\n", "\\n")
    }
}

/**
 * Replace default google credential factory by our own so we can store the json credentials on github with encrypted private key
 */
class CryptaxCredentialFactory : com.google.cloud.sql.CredentialFactory {
    override fun create(): Credential {
        val credential = GoogleCredential.fromStream(GcpConfig.googleCredentials().byteInputStream())
        return credential.createScoped(listOf(SQLAdminScopes.SQLSERVICE_ADMIN))
    }
}
