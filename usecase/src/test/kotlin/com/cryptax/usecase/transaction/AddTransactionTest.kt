package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.time.ZonedDateTime

@DisplayName("Usescase add a transaction to a user")
@ExtendWith(MockitoExtension::class)
class AddTransactionTest {

	@Mock
	lateinit var userRepository: UserRepository
	@Mock
	lateinit var transactionRepository: TransactionRepository
	@Mock
	lateinit var idGenerator: IdGenerator
	@InjectMocks
	lateinit var addTransaction: AddTransaction

	private val id = "random id"
	private val now = ZonedDateTime.now()
	private val userId = "userId"
	private val transaction = Transaction(
		userId = userId,
		source = Source.MANUAL,
		date = now,
		type = Transaction.Type.BUY,
		price = 10.0,
		amount = 5.0,
		currency1 = Currency.ETH,
		currency2 = Currency.BTC)
	private val transactionWithId = Transaction(
		id = id,
		userId = userId,
		source = Source.MANUAL,
		date = now,
		type = Transaction.Type.BUY,
		price = 10.0,
		amount = 5.0,
		currency1 = Currency.ETH,
		currency2 = Currency.BTC)
	private val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John")

	@Test
	fun testAdd() {
		// given
		given(idGenerator.generate()).willReturn(id)
		given(userRepository.findById(transaction.userId)).willReturn(user)
		given(transactionRepository.add(any())).willReturn(transactionWithId)

		// when
		val actual = addTransaction.add(transaction)

		// then
		assertEquals(transactionWithId, actual)
		then(userRepository).should().findById(transaction.userId)
		then(idGenerator).should().generate()
		then(transactionRepository).should().add(transactionWithId)
	}

	@Test
	fun testAddUserNotFound() {
		// given
		given(userRepository.findById(transaction.userId)).willReturn(null)

		// when
		val exception = assertThrows(UserNotFoundException::class.java) {
			addTransaction.add(transaction)
		}

		// then
		assertEquals(userId, exception.message)
		then(userRepository).should().findById(transaction.userId)
		then(userRepository).shouldHaveNoMoreInteractions()
		then(idGenerator).shouldHaveZeroInteractions()
		then(transactionRepository).shouldHaveZeroInteractions()
	}
}
