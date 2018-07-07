package com.cryptax.domain.entity

import java.time.LocalDateTime

data class Transaction(
	val id: String,
	val userId: String,
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


