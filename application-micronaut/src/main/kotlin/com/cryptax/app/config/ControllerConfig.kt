package com.cryptax.app.config

import com.cryptax.controller.CurrencyController
import com.cryptax.controller.ReportController
import com.cryptax.controller.SourceController
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
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class ControllerConfig {

    @Singleton
    fun userController(createUser: CreateUser, findUser: FindUser, loginUser: LoginUser, validateUser: ValidateUser, resetUserPassword: ResetUserPassword): UserController {
        return UserController(createUser, findUser, loginUser, validateUser, resetUserPassword)
    }

    @Singleton
    fun transactionController(addTransaction: AddTransaction, updateTransaction: UpdateTransaction, findTransaction: FindTransaction, deleteTransaction: DeleteTransaction): TransactionController {
        return TransactionController(addTransaction, updateTransaction, findTransaction, deleteTransaction)
    }

    @Singleton
    fun currencyController(): CurrencyController {
        return CurrencyController()
    }

    @Singleton
    fun reportController(generateReport: GenerateReport): ReportController {
        return ReportController(generateReport)
    }

    @Singleton
    fun sourceController(): SourceController {
        return SourceController()
    }
}
