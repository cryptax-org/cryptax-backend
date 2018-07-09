package com.cryptax.usecase

import com.cryptax.domain.entity.Transaction
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.ZoneId
import java.util.TimeZone

val objectMapper: ObjectMapper = ObjectMapper()
	.registerModule(KotlinModule())
	.registerModule(JavaTimeModule())
	.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
	.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
	.setSerializationInclusion(JsonInclude.Include.NON_NULL)

object Utils {
	const val id = "random id"
	val oneTransaction: Transaction = objectMapper.readValue(javaClass.getResourceAsStream("/transaction/OneTransaction.json"), Transaction::class.java)
	val oneTransactionExpected = Transaction(id, oneTransaction.userId, oneTransaction.source, oneTransaction.date, oneTransaction.type, oneTransaction.price, oneTransaction.amount, oneTransaction.currency1, oneTransaction.currency2)
	val twoTransactions: List<Transaction> = objectMapper.readValue(javaClass.getResourceAsStream("/transaction/TwoTransaction.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Transaction::class.java))
	val twoTransactionExpected = twoTransactions.map { Transaction(id, it.userId, it.source, it.date, it.type, it.price, it.amount, it.currency1, it.currency2) }
}

