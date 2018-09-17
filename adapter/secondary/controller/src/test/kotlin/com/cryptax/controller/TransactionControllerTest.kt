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
    fun `add a transaction`() {
        // given
        given(addTransaction.add(any())).willReturn(Single.just(transactionReturn))

        // when
        val actual = transactionController.addTransaction(userId, transactionWeb).blockingGet()

        // then
        assertThat(actual.id).isEqualTo(transactionId)
        assertThat(actual.source).isEqualTo(transactionReturn.source)
        assertThat(actual.date).isEqualTo(transactionReturn.date)
        assertThat(actual.type).isEqualTo(transactionReturn.type)
        assertThat(actual.price).isEqualTo(transactionReturn.price)
        assertThat(actual.quantity).isEqualTo(transactionReturn.quantity)
        assertThat(actual.currency1).isEqualTo(transactionReturn.currency1)
        assertThat(actual.currency2).isEqualTo(transactionReturn.currency2)
        argumentCaptor<Transaction>().apply {
            then(addTransaction).should().add(capture())
            assertThat(userId).isEqualTo(firstValue.userId)
        }
    }

    @Test
    fun `add multiple transactions`() {
        // given
        val transactionWeb2 = TransactionWeb(
            id = null,
            source = Source.MANUAL.name.toLowerCase(),
            date = now,
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 5.0,
            currency1 = Currency.BTC,
            currency2 = Currency.ETH)
        val transactionReturn2 = Transaction(
            id = "transactionid2",
            userId = userId,
            source = Source.MANUAL.name.toLowerCase(),
            date = now,
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 5.0,
            currency1 = Currency.BTC,
            currency2 = Currency.ETH)
        given(addTransaction.addMultiple(any())).willReturn(Single.just(listOf(transactionReturn, transactionReturn2)))

        // when
        val actual = transactionController.addMultipleTransactions(userId, listOf(transactionWeb, transactionWeb2)).blockingGet()

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].id).isEqualTo(transactionId)
        assertThat(actual[0].source).isEqualTo(transactionReturn.source)
        assertThat(actual[0].date).isEqualTo(transactionReturn.date)
        assertThat(actual[0].type).isEqualTo(transactionReturn.type)
        assertThat(actual[0].price).isEqualTo(transactionReturn.price)
        assertThat(actual[0].quantity).isEqualTo(transactionReturn.quantity)
        assertThat(actual[0].currency1).isEqualTo(transactionReturn.currency1)
        assertThat(actual[0].currency2).isEqualTo(transactionReturn.currency2)
        assertThat(actual[1].id).isEqualTo(transactionReturn2.id)
        assertThat(actual[1].source).isEqualTo(transactionReturn2.source)
        assertThat(actual[1].date).isEqualTo(transactionReturn2.date)
        assertThat(actual[1].type).isEqualTo(transactionReturn2.type)
        assertThat(actual[1].price).isEqualTo(transactionReturn2.price)
        assertThat(actual[1].quantity).isEqualTo(transactionReturn2.quantity)
        assertThat(actual[1].currency1).isEqualTo(transactionReturn2.currency1)
        assertThat(actual[1].currency2).isEqualTo(transactionReturn2.currency2)
        argumentCaptor<List<Transaction>>().apply {
            then(addTransaction).should().addMultiple(capture())
            assertThat(userId).isEqualTo(firstValue[0].userId)
            assertThat(userId).isEqualTo(firstValue[1].userId)
        }
    }

    @Test
    fun `update a transaction`() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(updateTransaction.update(transactionReturn)).willReturn(Single.just(transactionReturn))

        // when
        val actual = transactionController.updateTransaction(transactionId, userId, transactionWeb).blockingGet()

        // then
        assertThat(actual.id).isEqualTo(transactionId)
        assertThat(actual.source).isEqualTo(transactionReturn.source)
        assertThat(actual.date).isEqualTo(transactionReturn.date)
        assertThat(actual.type).isEqualTo(transactionReturn.type)
        assertThat(actual.price).isEqualTo(transactionReturn.price)
        assertThat(actual.quantity).isEqualTo(transactionReturn.quantity)
        assertThat(actual.currency1).isEqualTo(transactionReturn.currency1)
        assertThat(actual.currency2).isEqualTo(transactionReturn.currency2)
        argumentCaptor<Transaction>().apply {
            then(updateTransaction).should().update(capture())
            assertThat(userId).isEqualTo(firstValue.userId)
            assertThat(transactionId).isEqualTo(firstValue.id)
        }
    }

    @Test
    fun `get a transaction`() {
        // given
        val transactionId = "randomId"
        val userId = "userId"
        given(findTransaction.find(transactionId, userId)).willReturn(Maybe.just(transactionReturn))

        // when
        val actual = transactionController.getTransaction(transactionId, userId).blockingGet()

        // then
        assertThat(actual).isNotNull
        assertThat(actual!!.id).isEqualTo(transactionId)
        assertThat(actual.source).isEqualTo(transactionReturn.source)
        assertThat(actual.date).isEqualTo(transactionReturn.date)
        assertThat(actual.type).isEqualTo(transactionReturn.type)
        assertThat(actual.price).isEqualTo(transactionReturn.price)
        assertThat(actual.quantity).isEqualTo(transactionReturn.quantity)
        assertThat(actual.currency1).isEqualTo(transactionReturn.currency1)
        assertThat(actual.currency2).isEqualTo(transactionReturn.currency2)
        then(findTransaction).should().find(transactionId, userId)
    }

    @Test
    fun `get a transaction, not found`() {
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
    fun `get all transactions`() {
        // given
        val userId = "userId"
        given(findTransaction.findAllForUser(userId)).willReturn(Single.just(listOf(transactionReturn)))

        // when
        val actual = transactionController.getAllTransactions(userId).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0].id).isEqualTo(transactionId)
        assertThat(actual[0].source).isEqualTo(transactionReturn.source)
        assertThat(actual[0].date).isEqualTo(transactionReturn.date)
        assertThat(actual[0].type).isEqualTo(transactionReturn.type)
        assertThat(actual[0].price).isEqualTo(transactionReturn.price)
        assertThat(actual[0].quantity).isEqualTo(transactionReturn.quantity)
        assertThat(actual[0].currency1).isEqualTo(transactionReturn.currency1)
        assertThat(actual[0].currency2).isEqualTo(transactionReturn.currency2)
        then(findTransaction).should().findAllForUser(userId)
    }

    @Test
    fun `upload csv`() {
        // when
        val actual = assertThrows<RuntimeException> {
            transactionController.uploadCSVTransactions(inputStream, "", Source.MANUAL)
        }

        // then
        assertThat(actual.message).isEqualTo("Source [MANUAL] not handled")
        then(addTransaction).shouldHaveZeroInteractions()
    }

    @Test
    fun `delete a transaction`() {
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
