package com.cryptax.domain.exception

class LoginException(val email: String, val description: String) : RuntimeException()

class UserNotFoundException(id: String) : RuntimeException(id)

class UserAlreadyExistsException(email: String) : RuntimeException(email)

class UserValidationException(message: String) : RuntimeException(message)

class TransactionValidationException(message: String) : RuntimeException(message)
