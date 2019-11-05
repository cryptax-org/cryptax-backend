package com.cryptax.controller.validation

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.model.UserWeb
import io.reactivex.Observable
import io.reactivex.Single

object Validation {

    fun validateCreateUser(userWeb: UserWeb): Single<UserWeb> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (userWeb.email.isBlank()) {
                errors.add("Email can not be empty")
            }
            if (userWeb.password == null || userWeb.password.isEmpty()) {
                errors.add("Password can not be empty")
            }
            if (userWeb.lastName.isBlank()) {
                errors.add("Last name can not be empty")
            }
            if (userWeb.firstName.isBlank()) {
                errors.add("First name can not be empty")
            }
            if (errors.isEmpty()) {
                userWeb
            } else {
                throw ValidationException(errors)
            }
        }
    }

    fun validateRestPassword(email: String?, password: CharArray?, token: String?): Single<Triple<String, CharArray, String>> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (email == null || email.isBlank()) {
                errors.add("Email can not be empty")
            }
            if (password == null || password.isEmpty()) {
                errors.add("Password can not be empty")
            }
            if (token == null || token.isBlank()) {
                errors.add("Token can not be empty")
            }
            if (errors.isEmpty()) {
                Triple(email!!, password!!, token!!)
            } else {
                throw ValidationException(errors)
            }
        }
    }

    fun validateLogin(email: String?, password: CharArray? ): Single<Pair<String, CharArray>> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (email == null || email.isBlank()) {
                errors.add("Email can not be empty")
            }
            if (password == null || password.isEmpty()) {
                errors.add("Password can not be empty")
            }
            if (errors.isEmpty()) {
                Pair(email!!, password!!)
            } else {
                throw ValidationException(errors)
            }
        }
    }

    fun validateTransaction(transaction: TransactionWeb): Single<TransactionWeb> {
        return Single.fromCallable {
            val errors = mutableListOf<String>()
            if (transaction.source == null || transaction.source!!.isBlank()) {
                errors.add("Source can not be empty")
            }
            if (transaction.date == null) {
                errors.add("Date can not be null")
            }
            if (transaction.type == null) {
                errors.add("Type can not be null")
            }
            if (transaction.price == null) {
                errors.add("Price can not be null")
            }
            if (transaction.quantity == null) {
                errors.add("Quantity can not be null")
            }
            if (transaction.currency1 == null) {
                errors.add("Currency1 can not be null")
            }
            if (transaction.currency2 == null) {
                errors.add("Currency2 can not be null")
            }
            if (errors.isEmpty()) {
                transaction
            } else {
                throw ValidationException(errors)
            }
        }
    }

    fun validateTransactions(transactions: List<TransactionWeb>): Single<List<TransactionWeb>> {
        return Observable.fromIterable(transactions)
            .flatMap { transaction -> validateTransaction(transaction).toObservable() }
            .toList()
    }
}

class ValidationException(val errors: List<String>) : RuntimeException()
