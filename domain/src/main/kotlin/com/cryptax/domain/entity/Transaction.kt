package com.cryptax.domain.entity

import java.time.LocalDateTime

data class Transaction(
	val id: String? = null,
	val userId: String,
	val source: Source,
	val date: LocalDateTime,
	val type: Type,
	val price: Double,
	val amount: Double,
	val currency1: Currency,
	val currency2: Currency) {

	enum class Type {
		BUY, SELL
	}
}


