package com.cryptax.domain.port

import com.cryptax.domain.entity.Transaction

interface PriceService {
    fun getUsdAmount(transaction: Transaction): Pair<String?, Double>
}
