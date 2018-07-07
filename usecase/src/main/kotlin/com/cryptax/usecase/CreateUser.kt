package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.UserValidator
import java.util.Arrays

class CreateUser(private val repository: UserRepository, private val securePassword: SecurePassword, private val idGenerator: IdGenerator) {

	fun create(user: User): User {
		UserValidator.validateCreateUser(user)
		repository.findByEmail(user.email)?.run {
			throw UserAlreadyExistsException(user.email)
		}

		val userToSave = User(
			id = idGenerator.generate(),
			email = user.email,
			password = securePassword.securePassword(user.password).toCharArray(),
			lastName = user.lastName,
			firstName = user.firstName
		)
		Arrays.fill(user.password, '\u0000')
		return repository.create(userToSave)
	}
}
