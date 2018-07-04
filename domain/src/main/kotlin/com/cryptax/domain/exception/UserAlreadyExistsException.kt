package com.cryptax.domain.exception

class UserAlreadyExistsException(email: String) : RuntimeException(email)
