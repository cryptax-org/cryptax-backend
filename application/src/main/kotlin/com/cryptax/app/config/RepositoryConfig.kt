package com.cryptax.app.config

import com.cryptax.config.AppProps
import com.cryptax.config.ProfileType.dev
import com.cryptax.config.ProfileType.local
import com.cryptax.config.ProfileType.prod
import com.cryptax.config.ProfileType.test
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class RepositoryConfig {

    @Autowired
    lateinit var properties: AppProps

    // Users
    @Profile(value = [local, test])
    @Bean
    fun inMemoryUserRepository(): UserRepository {
        return InMemoryUserRepository()
    }

    @Profile(value = [local, test])
    @Bean
    fun inMemoryResetPasswordRepository(): ResetPasswordRepository {
        return InMemoryResetPasswordRepository()
    }

    @Profile(value = [dev, prod])
    @Bean
    fun cloudStoreUserRepository(datastore: Datastore): UserRepository {
        return CloudDatastoreUserRepository(datastore)
    }

    @Profile(value = [dev, prod])
    @Bean
    fun dataStore(): Datastore {
        return DatastoreOptions.newBuilder()
            .setProjectId(properties.db.projectId)
            .setCredentials(GoogleCredentials.fromStream(properties.db.credentials().byteInputStream())).build()
            .service
    }

    @Profile(value = [dev, prod])
    @Bean
    fun cloudStoreResetPasswordRepository(): ResetPasswordRepository {
        return CloudDatastoreResetPasswordRepository(dataStore())
    }

    // Transactions
    @Profile(value = [local, test])
    @Bean
    fun inMemoryTransactionRepository(): TransactionRepository {
        return InMemoryTransactionRepository()
    }

    @Profile(value = [dev, prod])
    @Bean
    fun cloudTransactionRepository(datastore: Datastore): TransactionRepository {
        return CloudDatastoreTransactionRepository(datastore)
    }
}
