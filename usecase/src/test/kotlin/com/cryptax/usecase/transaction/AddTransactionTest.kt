package com.cryptax.usecase.transaction

import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.Utils.id
import com.cryptax.usecase.Utils.oneTransaction
import com.cryptax.usecase.Utils.oneTransactionExpected
import com.cryptax.usecase.Utils.twoTransactionExpected
import com.cryptax.usecase.Utils.twoTransactions
import com.nhaarman.mockitokotlin2.any
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

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

    private val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
    private val transaction = oneTransaction
    private val expected = oneTransactionExpected
    private val transactions = twoTransactions
    private val transactionsExcepted = twoTransactionExpected

    @Test
    fun testAdd() {
        // given
        given(idGenerator.generate()).willReturn(id)
        given(userRepository.findById(transaction.userId)).willReturn(Maybe.just(user))
        given(transactionRepository.add(any<Transaction>())).willReturn(Single.just(expected))

        // when
        val actual = addTransaction.add(transaction).blockingGet()

        // then
        assertThat(expected).isEqualTo(actual)
        then(userRepository).should().findById(transaction.userId)
        then(idGenerator).should().generate()
        then(transactionRepository).should().add(expected)
    }

    @Test
    fun testAddUserNotFound() {
        // given
        given(userRepository.findById(transaction.userId)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(UserNotFoundException::class.java) {
            addTransaction.add(transaction).blockingGet()
        }

        // then
        assertThat(transaction.userId).isEqualTo(exception.message)
        then(userRepository).should().findById(transaction.userId)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(idGenerator).shouldHaveZeroInteractions()
        then(transactionRepository).shouldHaveZeroInteractions()
    }

    @Test
    fun testAddSeveral() {
        // given
        given(idGenerator.generate()).willReturn(id)
        given(userRepository.findById(transaction.userId)).willReturn(Maybe.just(user))
        given(transactionRepository.add(any<List<Transaction>>())).willReturn(Single.just(transactions))

        // when
        val actual = addTransaction.addMultiple(transactions).blockingGet()

        // then
        assertThat(actual).hasSize(2)
        then(userRepository).should().findById(transaction.userId)
        then(idGenerator).should(times(transactions.size)).generate()
        then(transactionRepository).should().add(transactionsExcepted)
    }

    @Test
    fun testAddSeveralUserNotFound() {
        // given
        given(userRepository.findById(transaction.userId)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(UserNotFoundException::class.java) {
            addTransaction.addMultiple(transactions).blockingGet()
        }

        // then
        assertThat(transactions[0].userId).isEqualTo(exception.message)
        then(userRepository).should().findById(transaction.userId)
        then(userRepository).shouldHaveNoMoreInteractions()
        then(idGenerator).shouldHaveZeroInteractions()
        then(transactionRepository).shouldHaveZeroInteractions()
    }
}
