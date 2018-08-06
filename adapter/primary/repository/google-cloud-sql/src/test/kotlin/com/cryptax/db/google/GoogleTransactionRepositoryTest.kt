package com.cryptax.db.google

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.nhaarman.mockitokotlin2.any
import org.assertj.core.api.Assertions.assertThat
import org.jooq.CreateTableAsStep
import org.jooq.CreateTableColumnStep
import org.jooq.DSLContext
import org.jooq.InsertSetStep
import org.jooq.InsertValuesStep9
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.Result
import org.jooq.SelectConditionStep
import org.jooq.SelectWhereStep
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
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
    private val table = table(name("transaction"))
    private val idField = field(name("id"), SQLDataType.VARCHAR)
    private val userIdField = field(name("userId"), SQLDataType.VARCHAR)
    private val sourceField = field(name("source"), SQLDataType.VARCHAR)
    private val dateField = field(name("date"), SQLDataType.OFFSETDATETIME)
    private val typeField = field(name("type"), SQLDataType.VARCHAR)
    private val priceField = field(name("price"), SQLDataType.DOUBLE)
    private val quantityField = field(name("quantity"), SQLDataType.DOUBLE)
    private val currency1Field = field(name("currency1"), SQLDataType.VARCHAR)
    private val currency2Field = field(name("currency2"), SQLDataType.VARCHAR)

    private val date = ZonedDateTime.now(zoneId)
    private val transaction = Transaction("id", "userId", Source.MANUAL, date, Transaction.Type.BUY, 50.0, 3.0, Currency.ETH, Currency.USD)

    private lateinit var dslContext: DSLContext
    private lateinit var googleTransactionRepository: GoogleTransactionRepository

    @BeforeEach
    fun beforeEach() {
        dslContext = mock(DSLContext::class.java)
        val tableStep = mock(CreateTableAsStep::class.java) as CreateTableAsStep<Record>
        val columnStep = mock(CreateTableColumnStep::class.java)
        given(dslContext.createTableIfNotExists(table)).willReturn(tableStep)
        given(tableStep.columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)).willReturn(columnStep)
        googleTransactionRepository = GoogleTransactionRepository(dslContext)
    }

    @Test
    fun testAdd() {
        // given
        val insertStep = mock(InsertSetStep::class.java) as InsertSetStep<Record>
        val insertValues = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        val insertValues2 = mock(InsertValuesStep9::class.java) as InsertValuesStep9<Record, String, String, String, OffsetDateTime, String, Double, Double, String, String>
        given(dslContext.insertInto(table)).willReturn(insertStep)
        given(insertStep.columns(idField, userIdField, sourceField, dateField, typeField, priceField, quantityField, currency1Field, currency2Field)).willReturn(insertValues)
        given(insertValues.values("id", "userId", Source.MANUAL.name, OffsetDateTime.ofInstant(transaction.date.toInstant(), zoneId), Transaction.Type.BUY.name, 50.0, 3.0, Currency.ETH.name, Currency.USD.name)).willReturn(insertValues2)

        // when
        val actual = googleTransactionRepository.add(transaction).blockingGet()

        // then
        assertThat(actual).isNotNull
        then(dslContext).should().insertInto(table)
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

        given(dslContext.selectFrom(table)).willReturn(selectStep)
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
    fun testGetAllTransactions() {
        // given
        val transactionId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val result: Result<Record> = mock(Result::class.java) as Result<Record>
        val record = mock(Record::class.java)

        given(dslContext.selectFrom(table)).willReturn(selectStep)
        given(selectStep.where(userIdField.eq(transactionId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetch()).willReturn(result)
        // FIXME: This is not great
        given(result.map(any<RecordMapper<Record?, Transaction>>())).willReturn(listOf(transaction))
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
        val actual = googleTransactionRepository.getAllForUser(transactionId).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0]).isEqualTo(transaction)
    }


/*    @Test
    fun testFindByIdFail() {
        // given
        val userId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>

        given(dslContext.selectFrom(userTable)).willReturn(selectStep)
        given(selectStep.where(idField.eq(userId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(null)

        // when
        val actual = googleTransactionRepository.findById(userId).blockingGet()

        // then
        assertThat(actual).isNull()
    }*/
/*
    @Test
    fun testFindByEmail() {
        // given
        val expected = User("id", "email", "password".toCharArray(), "last", "first", false)
        val email = "email"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val record = mock(Record::class.java)

        given(dslContext.selectFrom(userTable)).willReturn(selectStep)
        given(selectStep.where(emailField.eq(email))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(record)
        given(record.get(idField, String::class.java)).willReturn("id")
        given(record.get(emailField, String::class.java)).willReturn("email")
        given(record.get(passwordField, String::class.java)).willReturn("password")
        given(record.get(lastNameField, String::class.java)).willReturn("last")
        given(record.get(firstNameField, String::class.java)).willReturn("first")
        given(record.get(allowedField, Boolean::class.java)).willReturn(false)

        // when
        val actual = googleTransactionRepository.findByEmail(email).blockingGet()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testFindByEmailFail() {
        // given
        val email = "email"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>

        given(dslContext.selectFrom(userTable)).willReturn(selectStep)
        given(selectStep.where(emailField.eq(email))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(null)

        // when
        val actual = googleTransactionRepository.findByEmail(email).blockingGet()

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun testUpdateUser() {
        // given
        val user = User("id", "email", "password".toCharArray(), "last", "first", false)
        val updateStep = mock(UpdateSetFirstStep::class.java) as UpdateSetFirstStep<Record>
        val updateStepMore = mock(UpdateSetMoreStep::class.java) as UpdateSetMoreStep<Record>
        val lastUpdate = mock(UpdateConditionStep::class.java) as UpdateConditionStep<Record>

        given(dslContext.update(userTable)).willReturn(updateStep)
        given(updateStep.set(emailField, user.email)).willReturn(updateStepMore)
        given(updateStepMore.set(passwordField, user.password.joinToString(separator = ""))).willReturn(updateStepMore)
        given(updateStepMore.set(lastNameField, user.lastName)).willReturn(updateStepMore)
        given(updateStepMore.set(firstNameField, user.firstName)).willReturn(updateStepMore)
        given(updateStepMore.set(allowedField, user.allowed)).willReturn(updateStepMore)
        given(updateStepMore.where(idField.eq(user.id))).willReturn(lastUpdate)

        // when
        val actual = googleTransactionRepository.updateUser(user).blockingGet()

        // then
        assertThat(actual).isEqualTo(user)
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
    }*/
}
