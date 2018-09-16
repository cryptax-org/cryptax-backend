package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.usecase.transaction.AddTransaction
import com.cryptax.usecase.transaction.DeleteTransaction
import com.cryptax.usecase.transaction.FindTransaction
import com.cryptax.usecase.transaction.UpdateTransaction
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.given
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.InputStream
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
        source = Source.MANUAL.name.toLowerCase(),
        date = now,
        type = Transaction.Type.BUY,
        price = 10.0,
        quantity = 5.0,
        currency1 = Currency.BTC,
        currency2 = Currency.ETH)
    private val transactionWeb = TransactionWeb(
        id = null,
        source = Source.MANUAL.name.toLowerCase(),
        date = now,
        type = Transaction.Type.BUY,
        price = 10.0,
        quantity = 5.0,
        currency1 = Currency.BTC,
        currency2 = Currency.ETH)

    @Mock
    lateinit var inputStream: InputStream
    @Mock
    lateinit var addTransaction: AddTransaction
    @Mock
    lateinit var updateTransaction: UpdateTransaction
    @Mock
    lateinit var findTransaction: FindTransaction
    @Mock
    lateinit var deleteTransaction: DeleteTransaction
    @InjectMocks
    lateinit var transactionController: TransactionController

    @Test
    fun testAddTransaction() {
        // given
        given(addTransaction.add(any())).willReturn(Single.just(transactionReturn))

        // when
        val actual = transactionController.addTransaction(userId, transactionWeb).blockingGet()

        // then
        assertThat(transactionId).isEqualTo(actual.id)
        assertThat(transactionReturn.source).isEqualTo(actual.source)
        assertThat(transactionReturn.date).isEqualTo(actual.date)
        assertThat(transactionReturn.type).isEqualTo(actual.type)
        assertThat(transactionReturn.price).isEqualTo(actual.price)
        assertThat(transactionReturn.quantity).isEqualTo(actual.quantity)
        assertThat(transactionReturn.currency1).isEqualTo(actual.currency1)
        assertThat(transactionReturn.currency2).isEqualTo(actual.currency2)
        argumentCaptor<Transaction>().apply {
            then(addTransaction).should().add(capture())
            assertThat(userId).isEqualTo(firstValue.userId)
        }
    }

    @Test
    fun testUpdateTransaction() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(updateTransaction.update(transactionReturn)).willReturn(Single.just(transactionReturn))

        // when
        val actual = transactionController.updateTransaction(transactionId, userId, transactionWeb).blockingGet()

        // then
        assertThat(transactionId).isEqualTo(actual.id)
        assertThat(transactionReturn.source).isEqualTo(actual.source)
        assertThat(transactionReturn.date).isEqualTo(actual.date)
        assertThat(transactionReturn.type).isEqualTo(actual.type)
        assertThat(transactionReturn.price).isEqualTo(actual.price)
        assertThat(transactionReturn.quantity).isEqualTo(actual.quantity)
        assertThat(transactionReturn.currency1).isEqualTo(actual.currency1)
        assertThat(transactionReturn.currency2).isEqualTo(actual.currency2)
        argumentCaptor<Transaction>().apply {
            then(updateTransaction).should().update(capture())
            assertThat(userId).isEqualTo(firstValue.userId)
            assertThat(transactionId).isEqualTo(firstValue.id)
        }
    }

    @Test
    fun testGetTransaction() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(findTransaction.find(transactionId, userId)).willReturn(Maybe.just(transactionReturn))

        // when
        val actual = transactionController.getTransaction(transactionId, userId).blockingGet()

        // then
        assertThat(actual).isNotNull
        assertThat(transactionId).isEqualTo(actual!!.id)
        assertThat(transactionReturn.source).isEqualTo(actual.source)
        assertThat(transactionReturn.date).isEqualTo(actual.date)
        assertThat(transactionReturn.type).isEqualTo(actual.type)
        assertThat(transactionReturn.price).isEqualTo(actual.price)
        assertThat(transactionReturn.quantity).isEqualTo(actual.quantity)
        assertThat(transactionReturn.currency1).isEqualTo(actual.currency1)
        assertThat(transactionReturn.currency2).isEqualTo(actual.currency2)
        then(findTransaction).should().find(transactionId, userId)
    }

    @Test
    fun testGetTransactionNotFound() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(findTransaction.find(transactionId, userId)).willReturn(Maybe.empty())

        // when
        val actual = transactionController.getTransaction(transactionId, userId).blockingGet()

        // then
        assertThat(actual).isNull()
        then(findTransaction).should().find(transactionId, userId)
    }

    @Test
    fun testGetAllTransactions() {
        // given
        val userId = "userId"
        given(findTransaction.findAllForUser(userId)).willReturn(Single.just(listOf(transactionReturn)))

        // when
        val actual = transactionController.getAllTransactions(userId).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(transactionId).isEqualTo(actual[0].id)
        assertThat(transactionReturn.source).isEqualTo(actual[0].source)
        assertThat(transactionReturn.date).isEqualTo(actual[0].date)
        assertThat(transactionReturn.type).isEqualTo(actual[0].type)
        assertThat(transactionReturn.price).isEqualTo(actual[0].price)
        assertThat(transactionReturn.quantity).isEqualTo(actual[0].quantity)
        assertThat(transactionReturn.currency1).isEqualTo(actual[0].currency1)
        assertThat(transactionReturn.currency2).isEqualTo(actual[0].currency2)
        then(findTransaction).should().findAllForUser(userId)
    }

    @Test
    fun testUploadCSVTransactions() {
        // when
        val actual = assertThrows<RuntimeException> {
            transactionController.uploadCSVTransactions(inputStream, "", Source.MANUAL)
        }

        // then
        assertThat(actual.message).isEqualTo("Source [MANUAL] not handled")
        then(addTransaction).shouldHaveZeroInteractions()
    }

    @Test
    fun testDeleteTransaction() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(deleteTransaction.delete(transactionId, userId)).willReturn(Single.just(Unit))

        // when
        val actual = transactionController.deleteTransaction(transactionId, userId).blockingGet()

        // then
        assertThat(actual).isNotNull
        assertThat(actual).isEqualTo(Unit)
        then(deleteTransaction).should().delete(transactionId, userId)
    }
}
