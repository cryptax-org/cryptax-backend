package com.cryptax.config.gcp

import com.cryptax.config.AppConfig
import com.cryptax.config.dto.decrypt
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.DatastoreOptions
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

class GcpConfig(private val projectId: String) {
    private fun googleCredentials(): String {
        return decrypt(IOUtils.toString(AppConfig::class.java.classLoader.getResourceAsStream("Cryptax-credentials-enc.json"), Charset.forName("UTF-8")))
    }

    fun datastoreOptions(): DatastoreOptions {
        return DatastoreOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(GoogleCredentials.fromStream(googleCredentials().byteInputStream())).build()
    }
}
