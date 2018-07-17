package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.SecurePassword
import com.cryptax.domain.port.UserRepository

class ValidateUser(private val userRepository: UserRepository, private val securePassword: SecurePassword) {

    fun validate(userId: String, welcomeToken: String): Boolean {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        val runtimeToken = securePassword.generateToken(user)
        return if (welcomeToken == runtimeToken) {
            val userToSave = User(id = user.id, email = user.email, firstName = user.firstName, lastName = user.lastName, password = user.password, allowed = true)
            userRepository.updateUser(userToSave)
            true
        } else {
            false
        }
    }
}
