package com.cryptax.usecase.user

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

class FindUser(private val repository: UserRepository) {

    fun findById(id: String): Maybe<User> {
        return repository.findById(id).observeOn(Schedulers.computation())
    }
}
