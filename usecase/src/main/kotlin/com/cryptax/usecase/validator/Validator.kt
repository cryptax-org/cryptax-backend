package com.cryptax.usecase.validator

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserValidationException
import io.reactivex.Observable
import io.reactivex.Single

fun validateCreateUser(user: User): Single<User> {
    return Single.create { emitter ->
        when {
            user.email.isBlank() -> emitter.onError(UserValidationException("Email should not be blank"))
            user.firstName.isBlank() -> emitter.onError(UserValidationException("First name should not be blank"))
            user.lastName.isBlank() -> emitter.onError(UserValidationException("Last name should not be blank"))
            else -> emitter.onSuccess(user)
        }
    }
}

fun validateAddTransaction(transaction: Transaction): Single<Transaction> {
    return Single.create { emitter ->
        when {
            transaction.price < 0 -> emitter.onError(TransactionValidationException("Price can't be negative"))
            transaction.quantity < 0 -> emitter.onError(TransactionValidationException("Quantity can't be negative"))
            transaction.currency1 == transaction.currency2 -> emitter.onError(TransactionValidationException("Currency1 and Currency2 can't be the same"))
            else -> emitter.onSuccess(transaction)
        }
    }
}

fun validateAddTransactions(transactions: List<Transaction>): Single<List<Transaction>> {
    return Single.create { emitter ->
        when {
            transactions.isEmpty() -> emitter.onError(TransactionValidationException("No transactions provided"))
            transactions.map { it.userId }.toSet().size != 1 -> emitter.onError(TransactionValidationException("All transactions must have the same user id"))
        }
        Observable.fromIterable(transactions)
            .flatMap { t -> validateAddTransaction(t).toObservable() }
            .subscribe({}, { onError -> emitter.onError(onError) })
        emitter.onSuccess(transactions)
    }
}

fun validateUpdateTransaction(transaction: Transaction): Single<Transaction> {
    return Single.create { emitter ->
        if (transaction.id == "DEFAULT") {
            emitter.onError(TransactionValidationException("Id can not be null"))
        } else {
            emitter.onSuccess(transaction)
        }
    }
}
