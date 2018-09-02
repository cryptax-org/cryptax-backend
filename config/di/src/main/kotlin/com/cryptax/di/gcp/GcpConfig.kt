package com.cryptax.di.gcp

import com.cryptax.config.DbDProps
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.DatastoreOptions

class GcpConfig(private val db: DbDProps) {
    fun datastoreOptions(): DatastoreOptions {
        return DatastoreOptions.newBuilder()
            .setProjectId(db.projectId)
            .setCredentials(GoogleCredentials.fromStream(db.credentials().byteInputStream())).build()
    }
}
