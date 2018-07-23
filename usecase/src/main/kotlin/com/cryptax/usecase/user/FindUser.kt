package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe

class FindUser(private val repository: UserRepository) {

    fun findById(id: String): Maybe<User> {
        return repository.findById(id)
    }
}
