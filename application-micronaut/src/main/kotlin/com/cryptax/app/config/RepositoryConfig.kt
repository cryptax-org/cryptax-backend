package com.cryptax.app.config

import com.cryptax.app.config.ProfileType.dev
import com.cryptax.app.config.ProfileType.test
import com.cryptax.app.config.ProfileType.local
import com.cryptax.app.config.ProfileType.prod
import com.cryptax.config.AppProps
import com.cryptax.db.InMemoryResetPasswordRepository
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.db.cloud.datastore.CloudDatastoreResetPasswordRepository
import com.cryptax.db.cloud.datastore.CloudDatastoreTransactionRepository
import com.cryptax.db.cloud.datastore.CloudDatastoreUserRepository
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import javax.inject.Inject
import javax.inject.Singleton

@Factory
class RepositoryConfig {

    @Inject
    lateinit var properties: AppProps

    // Users
    @Requires(env = [local, test])
    @Singleton
    fun inMemoryUserRepository(): UserRepository {
        return InMemoryUserRepository()
    }

    @Requires(env = [local, test])
    @Singleton
    fun inMemoryResetPasswordRepository(): ResetPasswordRepository {
        return InMemoryResetPasswordRepository()
    }

    @Requires(env = [dev, prod])
    @Singleton
    fun cloudStoreUserRepository(datastore: Datastore): UserRepository {
        return CloudDatastoreUserRepository(datastore)
    }

    @Requires(env = [dev, prod])
    @Singleton
    fun dataStore(): Datastore {
        return DatastoreOptions.newBuilder()
            .setProjectId(properties.db.projectId)
            .setCredentials(GoogleCredentials.fromStream(properties.db.credentials().byteInputStream())).build()
            .service
    }

    @Requires(env = [dev, prod])
    @Singleton
    fun cloudStoreResetPasswordRepository(): ResetPasswordRepository {
        return CloudDatastoreResetPasswordRepository(dataStore())
    }

    // Transactions
    @Requires(env = [local, test])
    @Singleton
    fun inMemoryTransactionRepository(): TransactionRepository {
        return InMemoryTransactionRepository()
    }

    @Requires(env = [dev, prod])
    @Singleton
    fun cloudTransactionRepository(datastore: Datastore): TransactionRepository {
        return CloudDatastoreTransactionRepository(datastore)
    }
}
