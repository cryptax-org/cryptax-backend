package com.cryptax.domain.entity

open class User(
	val id: String?,
	val email: String,
	val password: CharArray,
	val lastName: String,
	val firstName: String
)
