package com.cryptax.controller.model

import com.cryptax.domain.entity.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserWeb(
	val id: String,
	val email: String,
	val password: String,
	val lastName: String,
	val firstName: String) {

	fun toUser(): User {
		return User(
			id = id,
			email = email,
			password = password,
			lastName = lastName,
			firstName = firstName)
	}

	companion object {

		fun toUserWeb(user: User): UserWeb {
			return UserWeb(id = user.id,
				email = user.email,
				password = user.password,
				lastName = user.lastName,
				firstName = user.firstName)
		}
	}
}
