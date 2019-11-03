package com.cryptax.app.micronaut.route

import com.cryptax.domain.port.UserRepository
import io.micronaut.context.ApplicationContext
import javax.inject.Singleton

@Singleton
class Derp(var context: ApplicationContext, var userRepository: UserRepository) {

    init {
        println("Derp init")
    }
}
