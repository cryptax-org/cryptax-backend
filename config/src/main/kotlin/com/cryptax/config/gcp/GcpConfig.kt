package com.cryptax.config.gcp

import com.cryptax.config.dto.DbDto
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.DatastoreOptions

class GcpConfig(private val db: DbDto) {
    fun datastoreOptions(): DatastoreOptions {
        return DatastoreOptions.newBuilder()
            .setProjectId(db.projectId)
            .setCredentials(GoogleCredentials.fromStream(db.credentials().byteInputStream())).build()
    }
}
