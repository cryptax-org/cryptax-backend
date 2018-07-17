package com.cryptax.security

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.SecurePassword
import com.cryptax.security.encoder.Encoder
import com.cryptax.security.encoder.Sha3512Encoder
import com.cryptax.security.util.str
import java.security.SecureRandom
import java.util.Base64

class SecurePassword(private val encoder: Encoder = Sha3512Encoder()) : SecurePassword {

    private val secureRandom = SecureRandom()

    override fun securePassword(password: CharArray): String {
        val hashedPassword = encoder.encode(password.str())
        val hashedSalt = generateSalt()
        return hashedSalt + DELIMITER + encoder.encode("$hashedPassword$hashedSalt")
    }

    override fun matchPassword(challengingPassword: CharArray, hashedSaltPassword: CharArray): Boolean {
        val hashedChallengingPassword = encoder.encode(challengingPassword.str())
        val hashedSalt = hashedSaltPassword.str().substringBefore(DELIMITER)
        val hashedPassword = hashedSaltPassword.str().substringAfter(DELIMITER)
        val hashToCompare = encoder.encode(hashedChallengingPassword + hashedSalt)
        return hashToCompare == hashedPassword
    }

    override fun generateToken(user: User): String {
        return encoder.encode(user.id + user.email + user.lastName + user.firstName)
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(20)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    companion object {
        private const val DELIMITER = "_"
    }
}
