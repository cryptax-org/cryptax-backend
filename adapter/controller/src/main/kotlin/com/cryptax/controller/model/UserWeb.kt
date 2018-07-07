package com.cryptax.controller.model

import com.cryptax.domain.entity.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserWeb(
	val id: String? = null,
	val email: String,
	private val password: String? = null,
	val lastName: String,
	val firstName: String) {

	fun toUser(): User {
		return User(
			id = id,
			email = email,
			password = password!!.toCharArray(),
			lastName = lastName,
			firstName = firstName)
	}

	companion object {

		fun toUserWeb(user: User): UserWeb {
			return UserWeb(id = user.id,
				email = user.email,
				lastName = user.lastName,
				firstName = user.firstName)
		}
	}
}
