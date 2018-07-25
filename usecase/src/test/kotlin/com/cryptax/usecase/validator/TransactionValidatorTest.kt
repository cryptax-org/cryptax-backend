package com.cryptax.usecase.validator

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.usecase.Utils.twoTransactions
import com.cryptax.usecase.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.ZonedDateTime
import java.util.stream.Stream

@DisplayName("Transaction data validation")
class TransactionValidatorTest {

    @Test
    fun testValidateTransaction() {
        //given
        val transaction = Transaction(
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            amount = 5.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        //when
        validateAddTransaction(transaction).blockingGet()

        //then
        // no failure
    }

    @ParameterizedTest
    @MethodSource("transactionProvider")
    fun testValidateTransactionFail(transaction: Transaction, errorMessage: String) {
        //when
        val exception = assertThrows(TransactionValidationException::class.java) {
            validateAddTransaction(transaction).blockingGet()
        }

        //then
        assertEquals(errorMessage, exception.message)
    }

    @Test
    fun testValidateTransactions() {
        //given
        val transaction = twoTransactions
        //when
        validateAddTransactions(transaction)

        //then
        // no failure
    }

    @ParameterizedTest
    @MethodSource("transactionsProvider")
    fun testValidateTransactionsFail(transactions: List<Transaction>, errorMessage: String) {
        //when
        val exception = assertThrows(TransactionValidationException::class.java) {
            validateAddTransactions(transactions).blockingGet()
        }

        //then
        assertEquals(errorMessage, exception.message)
    }

    companion object {

        @JvmStatic
        fun transactionProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Transaction(null, "userId", Source.MANUAL, ZonedDateTime.now(), Transaction.Type.BUY, -10.0, 4.0, Currency.ETH, Currency.BTC), "Price can't be negative"),
                Arguments.of(Transaction(null, "userId", Source.MANUAL, ZonedDateTime.now(), Transaction.Type.BUY, 10.0, -4.0, Currency.ETH, Currency.BTC), "Amount can't be negative"),
                Arguments.of(Transaction(null, "userId", Source.MANUAL, ZonedDateTime.now(), Transaction.Type.BUY, 10.0, 4.0, Currency.ETH, Currency.ETH), "Currency1 and Currency2 can't be the same")
            )
        }

        @JvmStatic
        fun transactionsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Transaction>(), "No transactions provided"),
                Arguments.of(
                    objectMapper.readValue(this::class.java.getResourceAsStream("/transaction/batch/Test1.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Transaction::class.java)),
                    this::class.java.getResourceAsStream("/transaction/batch/Test1-output").bufferedReader().use { it.readLine() }),
                Arguments.of(
                    objectMapper.readValue(this::class.java.getResourceAsStream("/transaction/batch/Test2.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Transaction::class.java)),
                    this::class.java.getResourceAsStream("/transaction/batch/Test2-output").bufferedReader().use { it.readLine() })
            )
        }
    }
}
