package com.cryptax.app.config

import com.cryptax.controller.CurrencyController
import com.cryptax.controller.ReportController
import com.cryptax.controller.TransactionController
import com.cryptax.controller.UserController
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
class ControllerConfig {

    @Bean
    fun userController(createUser: CreateUser, findUser: FindUser, loginUser: LoginUser, validateUser: ValidateUser, resetUserPassword: ResetUserPassword): UserController {
        return UserController(createUser, findUser, loginUser, validateUser, resetUserPassword)
    }

    @Bean
    fun transactionController(addTransaction: AddTransaction, updateTransaction: UpdateTransaction, findTransaction: FindTransaction, deleteTransaction: DeleteTransaction): TransactionController {
        return TransactionController(addTransaction, updateTransaction, findTransaction, deleteTransaction)
    }

    @Bean
    fun currencyController(): CurrencyController {
        return CurrencyController()
    }

    @Bean
    fun reportController(generateReport: GenerateReport): ReportController {
        return ReportController(generateReport)
    }
}
