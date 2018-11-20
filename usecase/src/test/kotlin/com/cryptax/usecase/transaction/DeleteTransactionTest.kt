package com.cryptax.usecase.transaction

import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionUserDoNotMatch
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.usecase.Utils.oneTransactionWithId
import com.cryptax.usecase.Utils.oneTransactionWithId2
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Usescase delete transaction")
@ExtendWith(MockitoExtension::class)
class DeleteTransactionTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository
    @InjectMocks
    lateinit var deleteTransaction: DeleteTransaction

    @Test
    fun `delete transaction`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.just(transaction))
        given(transactionRepository.delete(transaction.id)).willReturn(Single.just(Unit))

        // when
        val actual = deleteTransaction.delete(transaction.id, transaction.userId).blockingGet()

        // then
        assertThat(actual).isEqualTo(Unit)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).should().delete(transaction.id)
    }

    @Test
    fun `delete transaction, wrong user`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.just(oneTransactionWithId2))

        // when
        val exception = assertThrows(TransactionUserDoNotMatch::class.java) {
            deleteTransaction.delete(transaction.id, transaction.userId).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo("User [userId] tried to update [ffwefewfwefgerge], but that transaction is owned by [userId2]")
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).shouldHaveZeroInteractions()
    }

    @Test
    fun `delete transaction, transaction not found`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.empty())

        // when
        val exception = assertThrows(TransactionNotFound::class.java) {
            deleteTransaction.delete(transaction.id, transaction.userId).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo(transaction.id)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `delete transaction, fails`() {
        // given
        val transaction = oneTransactionWithId
        given(transactionRepository.get(transaction.id)).willReturn(Maybe.just(transaction))
        given(transactionRepository.delete(transaction.id)).willThrow(RuntimeException("failure"))

        // when
        assertThrows(RuntimeException::class.java) {
            deleteTransaction.delete(transaction.id, transaction.userId).blockingGet()
        }

        // then
        //assertThat(exception.message).isEqualTo(transaction.id)
        then(transactionRepository).should().get(transaction.id)
        then(transactionRepository).should().delete(transaction.id)
    }
}
