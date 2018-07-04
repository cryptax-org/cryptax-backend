package com.cryptax.domain.exception

class NotAllowedException(message: String) : RuntimeException(message)

class UserAlreadyExistsException(email: String) : RuntimeException(email)

class UserValidationException(message: String) : RuntimeException(message)
