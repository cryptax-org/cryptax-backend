package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.given
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime

@DisplayName("Transaction controller")
@ExtendWith(MockitoExtension::class)
class TransactionControllerTest {

	private val userId = "userId"
	private val transactionId = "randomId"
	private val now = ZonedDateTime.now()
	private val transactionReturn = Transaction(
		id = transactionId,
		userId = userId,
		source = Source.MANUAL,
		date = now,
		type = Transaction.Type.BUY,
		price = 10.0,
		amount = 5.0,
		currency1 = Currency.BTC,
		currency2 = Currency.ETH)
	private val transactionWeb = TransactionWeb(
		source = Source.MANUAL,
		date = now,
		type = Transaction.Type.BUY,
		price = 10.0,
		amount = 5.0,
		currency1 = Currency.BTC,
		currency2 = Currency.ETH)

	@Mock
	lateinit var addTransaction: AddTransaction
	@Mock
	lateinit var updateTransaction: UpdateTransaction
	@Mock
	lateinit var findTransaction: FindTransaction
	@InjectMocks
	lateinit var transactionController: TransactionController

	@Test
	fun testAddTransaction() {
		// given
		given(addTransaction.add(any())).willReturn(transactionReturn)

		// when
		val actual = transactionController.addTransaction(userId, transactionWeb)

		// then
		assertEquals(transactionId, actual.id)
		assertEquals(transactionReturn.source, actual.source)
		assertEquals(transactionReturn.date, actual.date)
		assertEquals(transactionReturn.type, actual.type)
		assertEquals(transactionReturn.price, actual.price)
		assertEquals(transactionReturn.amount, actual.amount)
		assertEquals(transactionReturn.currency1, actual.currency1)
		assertEquals(transactionReturn.currency2, actual.currency2)
		argumentCaptor<Transaction>().apply {
			then(addTransaction).should().add(capture())
			assertEquals(userId, firstValue.userId)
		}
	}

	@Test
	fun testUpdateTransaction() {
		// given
		val transactionId = "randomId"
		val userId = "userId"
		given(updateTransaction.update(transactionReturn)).willReturn(transactionReturn)

		// when
		val actual = transactionController.updateTransaction(transactionId, userId, transactionWeb)

		// then
		assertEquals(transactionId, actual.id)
		assertEquals(transactionReturn.source, actual.source)
		assertEquals(transactionReturn.date, actual.date)
		assertEquals(transactionReturn.type, actual.type)
		assertEquals(transactionReturn.price, actual.price)
		assertEquals(transactionReturn.amount, actual.amount)
		assertEquals(transactionReturn.currency1, actual.currency1)
		assertEquals(transactionReturn.currency2, actual.currency2)
		argumentCaptor<Transaction>().apply {
			then(updateTransaction).should().update(capture())
			assertEquals(userId, firstValue.userId)
			assertEquals(transactionId, firstValue.id)
		}
	}

	@Test
	fun testGetTransaction() {
		// given
		val transactionId = "randomId"
		val userId = "userId"
		given(findTransaction.find(transactionId, userId)).willReturn(transactionReturn)

		// when
		val actual = transactionController.getTransaction(transactionId, userId)

		// then
		assertNotNull(actual)
		assertEquals(transactionId, actual!!.id)
		assertEquals(transactionReturn.source, actual.source)
		assertEquals(transactionReturn.date, actual.date)
		assertEquals(transactionReturn.type, actual.type)
		assertEquals(transactionReturn.price, actual.price)
		assertEquals(transactionReturn.amount, actual.amount)
		assertEquals(transactionReturn.currency1, actual.currency1)
		assertEquals(transactionReturn.currency2, actual.currency2)
		then(findTransaction).should().find(transactionId, userId)
	}

	@Test
	fun testGetTransactionNotFound() {
		// given
		val transactionId = "randomId"
		val userId = "userId"
		given(findTransaction.find(transactionId, userId)).willReturn(null)

		// when
		val actual = transactionController.getTransaction(transactionId, userId)

		// then
		assertNull(actual)
		then(findTransaction).should().find(transactionId, userId)
	}

	@Test
	fun testGetAllTransactions() {
		// given
		val userId = "userId"
		given(findTransaction.findAllForUser(userId)).willReturn(listOf(transactionReturn))

		// when
		val actual = transactionController.getAllTransactions(userId)

		// then
		assert(actual.size == 1)
		assertEquals(transactionId, actual[0].id)
		assertEquals(transactionReturn.source, actual[0].source)
		assertEquals(transactionReturn.date, actual[0].date)
		assertEquals(transactionReturn.type, actual[0].type)
		assertEquals(transactionReturn.price, actual[0].price)
		assertEquals(transactionReturn.amount, actual[0].amount)
		assertEquals(transactionReturn.currency1, actual[0].currency1)
		assertEquals(transactionReturn.currency2, actual[0].currency2)
		then(findTransaction).should().findAllForUser(userId)
	}
}
