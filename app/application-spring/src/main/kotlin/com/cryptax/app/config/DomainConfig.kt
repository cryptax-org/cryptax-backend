package com.cryptax.app.config

import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.ResetPasswordRepository
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.report.GenerateReport
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.DeleteTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.cryptax.usecase.user.CreateUser
import com.cryptax.usecase.user.FindUser
import com.cryptax.usecase.user.LoginUser
import com.cryptax.usecase.user.ResetUserPassword
import com.cryptax.usecase.user.ValidateUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {

    // User
    @Bean
    fun createUser(userRepository: UserRepository, securePassword: SecurePassword, idGenerator: IdGenerator, emailService: EmailService): CreateUser {
        return CreateUser(
            repository = userRepository,
            securePassword = securePassword,
            idGenerator = idGenerator,
            emailService = emailService)
    }

    @Bean
    fun findUser(userRepository: UserRepository): FindUser {
        return FindUser(userRepository)
    }

    @Bean
    fun loginUser(userRepository: UserRepository, securePassword: SecurePassword): LoginUser {
        return LoginUser(userRepository, securePassword)
    }

    @Bean
    fun validateUser(userRepository: UserRepository, securePassword: SecurePassword): ValidateUser {
        return ValidateUser(userRepository, securePassword)
    }

    @Bean
    fun resetUserPassword(userRepository: UserRepository, resetPasswordRepository: ResetPasswordRepository, securePassword: SecurePassword, idGenerator: IdGenerator, emailService: EmailService): ResetUserPassword {
        return ResetUserPassword(
            idGenerator = idGenerator,
            securePassword = securePassword,
            emailService = emailService,
            userRepository = userRepository,
            resetPasswordRepository = resetPasswordRepository)
    }

    // Transaction
    @Bean
    fun addTransaction(transactionRepository: TransactionRepository, userRepository: UserRepository, idGenerator: IdGenerator): AddTransaction {
        return AddTransaction(transactionRepository, userRepository, idGenerator)
    }

    @Bean
    fun updateTransaction(transactionRepository: TransactionRepository): UpdateTransaction {
        return UpdateTransaction(transactionRepository)
    }

    @Bean
    fun findTransaction(transactionRepository: TransactionRepository): FindTransaction {
        return FindTransaction(transactionRepository)
    }

    @Bean
    fun deleteTransaction(transactionRepository: TransactionRepository): DeleteTransaction {
        return DeleteTransaction(transactionRepository)
    }

    // Report
    @Bean
    fun generateReport(userRepository: UserRepository, transactionRepository: TransactionRepository, priceService: PriceService): GenerateReport {
        return GenerateReport(userRepository, transactionRepository, priceService)
    }
}
