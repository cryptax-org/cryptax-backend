package com.cryptax.usecase.validator

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserValidationException

fun validateCreateUser(user: User) {
    if (user.email.isBlank()) throw UserValidationException("Email should not be blank")
    if (user.firstName.isBlank()) throw UserValidationException("First name should not be blank")
    if (user.lastName.isBlank()) throw UserValidationException("Last name should not be blank")
}

fun validateAddTransaction(transaction: Transaction) {
    if (transaction.price < 0) throw TransactionValidationException("Price can't be negative")
    if (transaction.amount < 0) throw TransactionValidationException("Amount can't be negative")
    if (transaction.currency1 == transaction.currency2) throw TransactionValidationException("Currency1 and Currency2 can't be the same")
}

fun validateAddTransactions(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        throw TransactionValidationException("No transactions provided")
    }
    transactions.forEach { validateAddTransaction(it) }
    if (transactions.map { it.userId }.toSet().size != 1) {
        throw TransactionValidationException("All transactions must have the same user id")
    }
}

fun validateUpdateTransaction(transaction: Transaction) {
    if (transaction.id == null) throw TransactionValidationException("Id can't be null")
}
