package com.cryptax.domain.port

import com.cryptax.domain.entity.ResetPassword
import io.reactivex.Maybe
import io.reactivex.Single

interface ResetPasswordRepository : Pingable {

    fun save(resetPassword: ResetPassword): Single<ResetPassword>

    fun findByUserId(userId: String): Maybe<ResetPassword>

    fun delete(userId: String): Single<Unit>
}
