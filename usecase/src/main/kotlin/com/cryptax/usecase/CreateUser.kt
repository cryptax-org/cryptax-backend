package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.PasswordEncoder
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.UserValidator

class CreateUser(private val repository: UserRepository, private val passwordEncoder: PasswordEncoder, private val idGenerator: IdGenerator) {

	fun create(user: User): User {
		UserValidator.validateCreateUser(user)
		if (repository.findByEmail(user.email).isPresent) {
			throw UserAlreadyExistsException(user.email)
		}
		val userToSave = User(
			id = idGenerator.generate(),
			email = user.email,
			password = passwordEncoder.encode(user.email + user.password),
			lastName = user.lastName,
			firstName = user.firstName
		)
		return repository.create(userToSave)
	}
}
