package com.cryptax.usecase.validator

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.usecase.Utils.oneTransaction
import com.cryptax.usecase.Utils.twoTransactions
import com.cryptax.usecase.objectMapper
import org.assertj.core.api.Assertions.assertThat
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

    @DisplayName("Validate transaction")
    @Test
    fun `validate transaction`() {
        //given
        val transaction = Transaction(
            userId = "userId",
            source = "manual",
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 5.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        //when
        validateAddTransaction(transaction).blockingGet()

        //then
        // no failure
    }

    @ParameterizedTest
    @MethodSource("transactionProvider")
    fun `validate transaction fails`(transaction: Transaction, errorMessage: String) {
        //when
        val exception = assertThrows(TransactionValidationException::class.java) {
            validateAddTransaction(transaction).blockingGet()
        }

        //then
        assertThat(errorMessage).isEqualTo(exception.message)
    }

    @Test
    fun `validate transactions`() {
        //given
        val transaction = twoTransactions
        //when
        validateAddTransactions(transaction).blockingGet()

        //then
        // no failure
    }

    @ParameterizedTest
    @MethodSource("transactionsProvider")
    fun `validate transactions fails`(transactions: List<Transaction>, errorMessage: String) {
        //when
        val exception = assertThrows(TransactionValidationException::class.java) {
            validateAddTransactions(transactions).blockingGet()
        }

        //then
        assertThat(errorMessage).isEqualTo(exception.message)
    }

    @Test
    fun testValidateUpdateTransaction() {
        // given
        val transaction = oneTransaction

        // when
        val exception = assertThrows(TransactionValidationException::class.java) {
            validateUpdateTransaction(transaction).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo("Id can not be null")
    }

    companion object {

        @JvmStatic
        fun transactionProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Transaction(userId = "userId", source = "manual", date = ZonedDateTime.now(), type = Transaction.Type.BUY, price = -10.0, quantity = 4.0, currency1 = Currency.ETH, currency2 = Currency.BTC), "Price can't be negative"),
                Arguments.of(Transaction(userId = "userId", source = "manual", date = ZonedDateTime.now(), type = Transaction.Type.BUY, price = 10.0, quantity = -4.0, currency1 = Currency.ETH, currency2 = Currency.BTC), "Quantity can't be negative"),
                Arguments.of(Transaction(userId = "userId", source = "manual", date = ZonedDateTime.now(), type = Transaction.Type.BUY, price = 10.0, quantity = 4.0, currency1 = Currency.ETH, currency2 = Currency.ETH), "Currency1 and Currency2 can't be the same")
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
