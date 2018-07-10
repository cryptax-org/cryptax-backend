package com.cryptax.usecase.transaction

import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.Utils.oneTransactionWithId
import com.cryptax.usecase.Utils.oneTransactionWithId2
import com.cryptax.usecase.Utils.twoTransactions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase find transaction")
@ExtendWith(MockitoExtension::class)
class FindTransactionTest {

	@Mock
	lateinit var transactionRepository: TransactionRepository
	@InjectMocks
	lateinit var findTransaction: FindTransaction

	@Test
	fun testFindTransaction() {
		// given
		val transaction = oneTransactionWithId
		given(transactionRepository.get(transaction.id!!)).willReturn(transaction)

		// when
		val actual = findTransaction.find(transaction.id!!, transaction.userId)

		// then
		assertEquals(transaction, actual)
		then(transactionRepository).should().get(transaction.id!!)
	}

	@Test
	fun testFindTransactionNotFound() {
		// given
		val transaction = oneTransactionWithId
		given(transactionRepository.get(transaction.id!!)).willReturn(null)

		// when
		val actual = findTransaction.find(transaction.id!!, transaction.userId)

		// then
		assertNull(actual)
		then(transactionRepository).should().get(transaction.id!!)
	}

	@Test
	fun testFindTransactionWrongUser() {
		// given
		val transaction = oneTransactionWithId
		val transactionReturned = oneTransactionWithId2
		val expected = "User [${transaction.userId}] tried to update [${transaction.id}], but that transaction is owned by [${transactionReturned.userId}]"
		given(transactionRepository.get(transaction.id!!)).willReturn(transactionReturned)

		// when
		val exception = assertThrows(TransactionUserDoNotMatch::class.java) {
			findTransaction.find(transaction.id!!, transaction.userId)
		}

		// then
		assertEquals(expected, exception.message)
		then(transactionRepository).should().get(transaction.id!!)
	}

	@Test
	fun testFindAllTransaction() {
		// given
		val transactions = twoTransactions
		given(transactionRepository.getAllForUser(transactions[0].userId)).willReturn(transactions)

		// when
		val actual = findTransaction.findAllForUser(transactions[0].userId)

		// then
		assertEquals(transactions, actual)
		then(transactionRepository).should().getAllForUser(transactions[0].userId)
	}
}
