package com.cryptax.domain.port

import com.cryptax.domain.entity.Transaction

interface PriceService {
    fun getPriceInDollars(transaction: Transaction): Double
}
