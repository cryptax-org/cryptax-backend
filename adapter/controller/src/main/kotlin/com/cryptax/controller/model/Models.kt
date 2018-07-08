package com.cryptax.controller.model

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import java.time.ZonedDateTime

class UserWeb(
	val id: String? = null,
	val email: String,
	private val password: CharArray? = null,
	val lastName: String,
	val firstName: String) {

	fun toUser(): User {
		return User(
			id = id,
			email = email,
			password = password!!,
			lastName = lastName,
			firstName = firstName)
	}

	companion object {

		fun toUserWeb(user: User): UserWeb {
			return UserWeb(id = user.id,
				email = user.email,
				lastName = user.lastName,
				firstName = user.firstName)
		}
	}
}

class TransactionWeb(
	val id: String? = null,
	val source: Source,
	val date: ZonedDateTime,
	val type: Transaction.Type,
	val price: Double,
	val amount: Double,
	val currency1: Currency,
	val currency2: Currency
) {

	fun toTransaction(userId: String): Transaction {
		return Transaction(
			id = id,
			userId = userId,
			source = source,
			date = date,
			type = type,
			price = price,
			amount = amount,
			currency1 = currency1,
			currency2 = currency2
		)
	}

	companion object {

		fun toTransactionWeb(transaction: Transaction): TransactionWeb {
			return TransactionWeb(
				id = transaction.id,
				source = transaction.source,
				date = transaction.date,
				type = transaction.type,
				price = transaction.price,
				amount = transaction.amount,
				currency1 = transaction.currency1,
				currency2 = transaction.currency2
			)
		}
	}
}

