package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.validator.validateCreateUser
import java.util.Arrays

class CreateUser(
    private val repository: UserRepository,
    private val securePassword: SecurePassword,
    private val idGenerator: IdGenerator,
    private val emailService: EmailService) {

    fun create(user: User): Pair<User, String> {
        validateCreateUser(user)
        repository.findByEmail(user.email)?.run {
            throw UserAlreadyExistsException(user.email)
        }

        val userToSave = User(
            id = idGenerator.generate(),
            email = user.email,
            password = securePassword.securePassword(user.password).toCharArray(),
            lastName = user.lastName,
            firstName = user.firstName,
            allowed = false
        )
        val welcomeToken = securePassword.generateToken(userToSave)
        Arrays.fill(user.password, '\u0000')
        emailService.welcomeEmail(userToSave, welcomeToken)
        return Pair(repository.create(userToSave), welcomeToken)
    }
}
