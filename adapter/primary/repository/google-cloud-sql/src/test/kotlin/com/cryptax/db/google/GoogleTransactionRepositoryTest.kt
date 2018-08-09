package com.cryptax.db.google

import com.cryptax.db.google.GoogleTransactionRepository.Companion.currency1Field
import com.cryptax.db.google.GoogleTransactionRepository.Companion.currency2Field
import com.cryptax.db.google.GoogleTransactionRepository.Companion.dateField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.idField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.priceField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.quantityField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.sourceField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.tableTransaction
import com.cryptax.db.google.GoogleTransactionRepository.Companion.typeField
import com.cryptax.db.google.GoogleTransactionRepository.Companion.userIdField
import com.cryptax.db.google.GoogleUserRepository.Companion.tableUser
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.nhaarman.mockitokotlin2.any
import org.assertj.core.api.Assertions.assertThat
import org.jooq.CreateTableAsStep
import org.jooq.CreateTableColumnStep
import org.jooq.CreateTableConstraintStep
import org.jooq.DSLContext
import org.jooq.InsertSetStep
import org.jooq.InsertValuesStep9
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.Result
import org.jooq.ResultQuery
import org.jooq.SelectConditionStep
import org.jooq.SelectWhereStep
import org.jooq.UpdateConditionStep
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Suppress("UNCHECKED_CAST")
@DisplayName("Google transaction repository test")
class GoogleTransactionRepositoryTest {

    // TODO extract that data from base code
    private val zoneId = ZoneId.of("UTC")
    private val date = ZonedDateTime.now(zoneId)
    private val transaction = Transaction("id", "userId", Source.MANUAL, date, Transaction.Type.BUY, 50.0, 3.0, Currency.ETH, Currency.USD)

    private lateinit var dslContext: DSLContext
    private lateinit var googleTransactionRepository: GoogleTransactionRepository

    @BeforeEach
    fun beforeEach() {
        dslContext = mock(DSLContext::class.java)
        val tableStep = mock(CreateTableAsStep::class.java) as CreateTableAsStep<Record>
        val columnStep = mock(CreateTableColumnStep::class.java)
        val constraintStep = mock(CreateTableConstraintStep::class.java)
        given(dslContext.createTableIfNotExists(tableTransaction)).willReturn(tableStep)
        given(tableStep.columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)).willReturn(columnStep)
        given(columnStep.constraints(constraint("PK_TRANSACTION").primaryKey(idField), constraint("FK_USER_ID_TRANSACTION").foreignKey(userIdField).references(tableUser, field(name("id"), SQLDataType.VARCHAR)))).willReturn(constraintStep)
        googleTransactionRepository = GoogleTransactionRepository(dslContext)
    }

    @Test
    fun testAdd() {
        // given
        val insertStep = mock(InsertSetStep::class.java) as InsertSetStep<Record>
        val insertValues = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        val insertValues2 = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        given(dslContext.insertInto(tableTransaction)).willReturn(insertStep)
        given(insertStep.columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)).willReturn(insertValues)
        given(insertValues.values("id", "userId", Source.MANUAL.name, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId), Transaction.Type.BUY.name, 50.0, 3.0, Currency.ETH.name, Currency.USD.name)).willReturn(insertValues2)

        // when
        val actual = googleTransactionRepository.add(transaction).blockingGet()

        // then
        assertThat(actual).isNotNull
        then(dslContext).should().insertInto(tableTransaction)
        then(insertStep).should().columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)
        then(insertValues).should().values("id", "userId", Source.MANUAL.name, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId), Transaction.Type.BUY.name, 50.0, 3.0, Currency.ETH.name, Currency.USD.name)
    }

    @Test
    fun testAddTransactions() {
        // given
        val insertStep = mock(InsertSetStep::class.java) as InsertSetStep<Record>
        val insertValues = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        val insertValues2 = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        given(dslContext.insertInto(tableTransaction)).willReturn(insertStep)
        given(insertStep.columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)).willReturn(insertValues)
        given(insertValues.values("id", "userId", Source.MANUAL.name, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId), Transaction.Type.BUY.name, 50.0, 3.0, Currency.ETH.name, Currency.USD.name)).willReturn(insertValues2)

        // when
        val actual = googleTransactionRepository.add(listOf(transaction)).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0]).isEqualTo(transaction)
        then(dslContext).should().insertInto(tableTransaction)
        then(insertStep).should().columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)
        then(insertValues).should().values("id", "userId", Source.MANUAL.name, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId), Transaction.Type.BUY.name, 50.0, 3.0, Currency.ETH.name, Currency.USD.name)
    }

    @Test
    fun testGet() {
        // given
        val transactionId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val record = mock(Record::class.java)

        given(dslContext.selectFrom(tableTransaction)).willReturn(selectStep)
        given(selectStep.where(idField.eq(transactionId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(record)
        given(record.get(idField, String::class.java)).willReturn("id")
        given(record.get(userIdField, String::class.java)).willReturn("userId")
        given(record.get(sourceField, String::class.java)).willReturn(Source.MANUAL.name)
        given(record.get(dateField, OffsetDateTime::class.java)).willReturn(OffsetDateTime.ofInstant(date.toInstant(), zoneId))
        given(record.get(typeField, String::class.java)).willReturn(Transaction.Type.BUY.name)
        given(record.get(priceField, Double::class.java)).willReturn(50.0)
        given(record.get(quantityField, Double::class.java)).willReturn(3.0)
        given(record.get(currency1Field, String::class.java)).willReturn(Currency.ETH.name)
        given(record.get(currency2Field, String::class.java)).willReturn(Currency.USD.name)

        // when
        val actual = googleTransactionRepository.get(transactionId).blockingGet()

        // then
        assertThat(actual).isEqualTo(transaction)
    }

    @Test
    fun testGetNotFound() {
        // given
        val transactionId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>

        given(dslContext.selectFrom(tableTransaction)).willReturn(selectStep)
        given(selectStep.where(idField.eq(transactionId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(null)

        // when
        val actual = googleTransactionRepository.get(transactionId).blockingGet()

        // then
        assertThat(actual).isEqualTo(null)
    }

    @Test
    fun testGetAllTransactions() {
        // given
        val transactionId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val result: Result<Record> = mock(Result::class.java) as Result<Record>

        given(dslContext.selectFrom(tableTransaction)).willReturn(selectStep)
        given(selectStep.where(userIdField.eq(transactionId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetch()).willReturn(result)
        given(result.isEmpty()).willReturn(false)
        given(result.map(any<RecordMapper<Record?, Transaction>>())).willReturn(listOf(transaction))

        // when
        val actual = googleTransactionRepository.getAllForUser(transactionId).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0]).isEqualTo(transaction)
    }

    @Test
    fun testGetAllTransactionsEmpty() {
        // given
        val transactionId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val result: Result<Record> = mock(Result::class.java) as Result<Record>

        given(dslContext.selectFrom(tableTransaction)).willReturn(selectStep)
        given(selectStep.where(userIdField.eq(transactionId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetch()).willReturn(result)
        given(result.isEmpty()).willReturn(false)
        given(result.map(any<RecordMapper<Record?, Transaction>>())).willReturn(listOf())

        // when
        val actual = googleTransactionRepository.getAllForUser(transactionId).blockingGet()

        // then
        assertThat(actual).isEmpty()
    }

    @Test
    fun testUpdateTransaction() {
        // given
        val updateStep = mock(UpdateSetFirstStep::class.java) as UpdateSetFirstStep<Record>
        val updateStepMore = mock(UpdateSetMoreStep::class.java) as UpdateSetMoreStep<Record>
        val lastUpdate = mock(UpdateConditionStep::class.java) as UpdateConditionStep<Record>

        given(dslContext.update(tableTransaction)).willReturn(updateStep)
        given(updateStep.set(userIdField, transaction.userId)).willReturn(updateStepMore)
        given(updateStepMore.set(sourceField, transaction.source.name)).willReturn(updateStepMore)
        given(updateStepMore.set(dateField, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId))).willReturn(updateStepMore)
        given(updateStepMore.set(typeField, transaction.type.name)).willReturn(updateStepMore)
        given(updateStepMore.set(priceField, transaction.price)).willReturn(updateStepMore)
        given(updateStepMore.set(quantityField, transaction.quantity)).willReturn(updateStepMore)
        given(updateStepMore.set(currency1Field, transaction.currency1.name)).willReturn(updateStepMore)
        given(updateStepMore.set(currency2Field, transaction.currency2.name)).willReturn(updateStepMore)
        given(updateStepMore.where(idField.eq(transaction.id))).willReturn(lastUpdate)

        // when
        val actual = googleTransactionRepository.update(transaction).blockingGet()

        // then
        assertThat(actual).isEqualTo(transaction)
    }

    @Test
    fun testPing() {
        // given
        val query = mock(ResultQuery::class.java) as ResultQuery<Record>
        val result = mock(Result::class.java) as Result<Record>
        given(dslContext.resultQuery("SELECT 1")).willReturn(query)
        given(query.fetch()).willReturn(result)
        given(result.isNotEmpty).willReturn(false)

        // when
        val actual = googleTransactionRepository.ping()

        // then
        assertThat(actual).isFalse()
    }
}
