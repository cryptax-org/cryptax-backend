package com.cryptax.db

import com.cryptax.domain.entity.Transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("In memory transaction repository test")
class InMemoryTransactionRepositoryTest {

    private lateinit var transaction1: Transaction
    private lateinit var transaction2: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionRepository: InMemoryTransactionRepository

    @BeforeEach
    fun setUp() {
        transactionRepository = InMemoryTransactionRepository()
    }

    @BeforeAll
    internal fun beforeAll() {
        transaction1 = objectMapper.readValue(this::class.java.getResourceAsStream("/Transaction1.json"), Transaction::class.java)
        transaction2 = objectMapper.readValue(this::class.java.getResourceAsStream("/Transaction2.json"), Transaction::class.java)
        transactions = objectMapper.readValue(this::class.java.getResourceAsStream("/Transactions1.json"), objectMapper.typeFactory.constructCollectionType(List::class.java, Transaction::class.java))
    }

    @DisplayName("Add a transaction")
    @Test
    fun testAddTransaction() {
        // when
        val actual = transactionRepository.add(transaction1).blockingGet()

        // then
        assertEquals(transaction1, actual)
    }

    @DisplayName("Add several transactions")
    @Test
    fun testAddSeveralTransactions() {
        // when
        val actual = transactionRepository.add(transactions).blockingGet()

        // then
        assert(actual.size == 2)
        assertEquals(transactions[0], actual[0])
        assertEquals(transactions[1], actual[1])
    }

    @DisplayName("Get a transaction")
    @Test
    fun testGetTransaction() {
        // given
        transactionRepository.add(transaction1).blockingGet()

        // when
        val actual = transactionRepository.get(transaction1.id).blockingGet()

        // then
        assertEquals(transaction1, actual)
    }

    @DisplayName("Add several transactions")
    @Test
    fun testGetAllForUser() {
        // given
        transactionRepository.add(transactions).blockingGet()

        // when
        val actual = transactionRepository.getAllForUser("userId2").blockingGet()

        // then
        assert(actual.size == 1)
        assertEquals(transactions[1], actual[0])
    }

    @DisplayName("Update a transaction")
    @Test
    fun testUpdateTransaction() {
        // given
        transactionRepository.add(transaction1).blockingGet()

        // when
        val actual = transactionRepository.update(transaction2).blockingGet()

        // then
        assertEquals(transaction2, actual)
    }
}
