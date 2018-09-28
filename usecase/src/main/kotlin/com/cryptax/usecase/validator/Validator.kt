package com.cryptax.usecase.validator

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserValidationException
import io.reactivex.Observable
import io.reactivex.Single

fun validateCreateUser(user: User): Single<User> {
    return Single.defer {
        when {
            user.email.isBlank() -> Single.error(UserValidationException("Email should not be blank"))
            user.firstName.isBlank() -> Single.error(UserValidationException("First name should not be blank"))
            user.lastName.isBlank() -> Single.error(UserValidationException("Last name should not be blank"))
            else -> Single.just(user)
        }
    }
}

fun validateAddTransaction(transaction: Transaction): Single<Transaction> {
    return Single.defer {
        when {
            transaction.price < 0 -> Single.error(TransactionValidationException("Price can't be negative"))
            transaction.quantity < 0 -> Single.error(TransactionValidationException("Quantity can't be negative"))
            transaction.currency1 == transaction.currency2 -> Single.error(TransactionValidationException("Currency1 and Currency2 can't be the same"))
            else -> Single.just(transaction)
        }
    }
}

fun validateAddTransactions(transactions: List<Transaction>): Single<List<Transaction>> {
    return Single.defer {
        when {
            transactions.isEmpty() -> throw TransactionValidationException("No transactions provided")
            transactions.map { it.userId }.toSet().size != 1 -> throw TransactionValidationException("All transactions must have the same user id")
        }
        Observable.fromIterable(transactions)
            .flatMap { t -> validateAddTransaction(t).toObservable() }
            .toList()
    }
}

fun validateUpdateTransaction(transaction: Transaction): Single<Transaction> {
    return Single.defer {
        if (transaction.id == "DEFAULT") {
            Single.error(TransactionValidationException("Id can not be null"))
        } else {
            Single.just(transaction)
        }
    }
}
