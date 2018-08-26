package com.cryptax.domain.port

import com.cryptax.domain.entity.Transaction
import io.reactivex.Maybe
import io.reactivex.Single

interface TransactionRepository : Pingable {

    fun add(transaction: Transaction): Single<Transaction>

    fun add(transactions: List<Transaction>): Single<List<Transaction>>

    fun get(id: String): Maybe<Transaction>

    fun getAllForUser(userId: String): Single<List<Transaction>>

    fun update(transaction: Transaction): Single<Transaction>

    fun delete(id: String): Single<Unit>
}
