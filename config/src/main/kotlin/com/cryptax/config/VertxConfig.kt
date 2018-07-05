package com.cryptax.config

import com.cryptax.controller.VertxUserController
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.encoder.Sha256PasswordEncoder
import com.cryptax.id.JugIdGenerator
import com.cryptax.usecase.CreateUser
import com.cryptax.usecase.FindUser
import com.cryptax.usecase.LoginUser

object VertxConfig {

	private val userRepository = InMemoryUserRepository()
	private val idGenerator = JugIdGenerator()
	private val passwordEncoder = Sha256PasswordEncoder()
	private val createUser = CreateUser(userRepository, passwordEncoder, idGenerator)
	private val findUser = FindUser(userRepository)
	private val loginUser = LoginUser(userRepository, passwordEncoder)
	val vertxUserController = VertxUserController(createUser, findUser, loginUser)
}
