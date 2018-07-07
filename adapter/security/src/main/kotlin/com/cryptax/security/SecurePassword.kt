package com.cryptax.security

import com.cryptax.domain.port.SecurePassword
import com.cryptax.security.encoder.Encoder
import com.cryptax.security.encoder.Sha256Encoder
import java.security.SecureRandom

class SecurePassword(private val encoder: Encoder = Sha256Encoder()) : SecurePassword {

	override fun securePassword(password: CharArray): String {
		val hashedPassword = encoder.encode(password.joinToString(""))
		val hashedSalt = generateSalt()
		return hashedSalt + encoder.encode("$hashedPassword$hashedSalt")
	}

	override fun matchPassword(challengingPassword: CharArray, hashedSaltPassword: CharArray): Boolean {
		val hashedChallengingPassword = encoder.encode(challengingPassword.joinToString(""))
		val hashedSalt = hashedSaltPassword.joinToString("").substring(IntRange(0, 63))
		val hashedPassword = hashedSaltPassword.joinToString("").substring(IntRange(64, hashedSaltPassword.size - 1))
		val hashToCompare = encoder.encode(hashedChallengingPassword + hashedSalt)
		return hashToCompare == hashedPassword
	}

	private fun generateSalt(): String {
		val random = SecureRandom()
		return encoder.encode(random.toString())
	}
}
