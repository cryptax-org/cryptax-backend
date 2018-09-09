package com.cryptax.app.config

import com.cryptax.app.config.ProfileType.dev
import com.cryptax.app.config.ProfileType.it
import com.cryptax.app.config.ProfileType.local
import com.cryptax.app.config.ProfileType.prod
import com.cryptax.config.AppProps
import com.cryptax.controller.UserController
import com.cryptax.db.InMemoryResetPasswordRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.db.cloud.datastore.CloudDatastoreResetPasswordRepository
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.cryptax.email.SendGridEmailService
import com.cryptax.id.JugIdGenerator
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ResetUserPassword
import com.cryptax.usecase.user.ValidateUser
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

@Configuration
class ControllerConfig {

    @Autowired
    lateinit var properties: AppProps

    @Profile(value = [local, it])
    @Bean
    fun inMemoryUserRepository(): UserRepository {
        return InMemoryUserRepository()
    }

    @Profile(value = [local, it])
    @Bean
    fun inMemoryResetPasswordRepository(): ResetPasswordRepository {
        return InMemoryResetPasswordRepository()
    }

    @Profile(value = [dev, prod])
    @Bean
    fun cloudStoreUserRepository(): UserRepository {
        return InMemoryUserRepository()
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

    @Bean
    fun securePassword(): SecurePassword {
        return com.cryptax.security.SecurePassword()
    }

    @Bean
    fun idGenerator(): IdGenerator {
        return JugIdGenerator()
    }

    @Bean
    fun httpClient(): OkHttpClient {
        val connectionPool = ConnectionPool(properties.http.maxIdleConnections, properties.http.keepAliveDuration, TimeUnit.MINUTES)
        val builder = OkHttpClient.Builder().connectionPool(connectionPool)
        return builder.build()
    }

    @Bean
    fun emailService(): EmailService {
        return SendGridEmailService(httpClient(), properties.email)
    }

    @Bean
    fun createUser(userRepository: UserRepository): CreateUser {
        return CreateUser(
            repository = userRepository,
            securePassword = securePassword(),
            idGenerator = idGenerator(),
            emailService = emailService()
        )
    }

    @Bean
    fun findUser(userRepository: UserRepository): FindUser {
        return FindUser(userRepository)
    }

    @Bean
    fun loginUser(userRepository: UserRepository): LoginUser {
        return LoginUser(userRepository, securePassword())
    }

    @Bean
    fun validateUser(userRepository: UserRepository): ValidateUser {
        return ValidateUser(userRepository, securePassword())
    }

    @Bean
    fun resetUserPassword(userRepository: UserRepository, resetPasswordRepository: ResetPasswordRepository): ResetUserPassword {
        return ResetUserPassword(
            idGenerator = idGenerator(),
            securePassword = securePassword(),
            emailService = emailService(),
            userRepository = userRepository,
            resetPasswordRepository = resetPasswordRepository)
    }

    @Bean
    fun userController(
        createUser: CreateUser,
        findUser: FindUser,
        loginUser: LoginUser,
        validateUser: ValidateUser,
        resetUserPassword: ResetUserPassword): UserController {
        return UserController(createUser, findUser, loginUser, validateUser, resetUserPassword)
    }
}
