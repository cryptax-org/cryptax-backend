package com.cryptax.domain.exception

abstract class CryptaxException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
}

abstract class ValidationException(message: String) : CryptaxException(message)

class UserValidationException(message: String) : ValidationException(message)

class TransactionValidationException(message: String) : ValidationException(message)

class LoginException(val email: String, val description: String) : CryptaxException()

class UserNotFoundException(id: String) : CryptaxException(id)

class UserAlreadyExistsException(email: String) : CryptaxException(email)

class TransactionNotFound(message: String) : CryptaxException(message)

class TransactionUserDoNotMatch(transactionUserId: String, transactionId: String, transactionUserIdFound: String)
    : CryptaxException("User [$transactionUserId] tried to update [$transactionId], but that transaction is owned by [$transactionUserIdFound]")

class ReportException(message: String) : CryptaxException(message)
