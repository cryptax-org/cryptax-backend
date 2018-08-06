package com.cryptax.db.google

import com.cryptax.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.jooq.CreateTableAsStep
import org.jooq.CreateTableColumnStep
import org.jooq.DSLContext
import org.jooq.InsertSetStep
import org.jooq.InsertValuesStep6
import org.jooq.Record
import org.jooq.Result
import org.jooq.ResultQuery
import org.jooq.SelectConditionStep
import org.jooq.SelectWhereStep
import org.jooq.UpdateConditionStep
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock

@Suppress("UNCHECKED_CAST")
@DisplayName("Google user repository test")
class GoogleUserRepositoryTest {

    // TODO extract that data from base code
    private val userTable = table(name("user"))
    private val idField = DSL.field(name("id"), SQLDataType.VARCHAR)
    private val emailField = DSL.field(name("email"), SQLDataType.VARCHAR)
    private val passwordField = DSL.field(name("password"), SQLDataType.VARCHAR)
    private val lastNameField = DSL.field(name("lastName"), SQLDataType.VARCHAR)
    private val firstNameField = DSL.field(name("firstName"), SQLDataType.VARCHAR)
    private val allowedField = DSL.field(name("allowed"), SQLDataType.BOOLEAN)

    private lateinit var dslContext: DSLContext
    private lateinit var googleUserRepository: GoogleUserRepository

    @BeforeEach
    fun beforeEach() {
        dslContext = mock(DSLContext::class.java)
        val tableStep = mock(CreateTableAsStep::class.java) as CreateTableAsStep<Record>
        val columnStep = mock(CreateTableColumnStep::class.java)
        given(dslContext.createTableIfNotExists(userTable)).willReturn(tableStep)
        given(tableStep.columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)).willReturn(columnStep)
        googleUserRepository = GoogleUserRepository(dslContext)
    }

    @Test
    fun testCreate() {
        // given
        val user = User("id", "email", "password".toCharArray(), "last", "first", false)
        val insertStep = mock(InsertSetStep::class.java) as InsertSetStep<Record>
        val insertValues = mock(InsertValuesStep6::class.java) as InsertValuesStep6<Record, String, String, String, String, String, Boolean>
        val insertValues2 = mock(InsertValuesStep6::class.java) as InsertValuesStep6<Record, String, String, String, String, String, Boolean>
        given(dslContext.insertInto(userTable)).willReturn(insertStep)
        given(insertStep.columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)).willReturn(insertValues)
        given(insertValues.values("id", "email", "password", "last", "first", false)).willReturn(insertValues2)

        // when
        val actual = googleUserRepository.create(user).blockingGet()

        // then
        assertThat(actual).isNotNull
        then(dslContext).should().insertInto(userTable)
        then(insertStep).should().columns(idField, emailField, passwordField, lastNameField, firstNameField, allowedField)
        then(insertValues).should().values("id", "email", "password", "last", "first", false)
    }

    @Test
    fun testFindById() {
        // given
        val expected = User("id", "email", "password".toCharArray(), "last", "first", false)
        val userId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>
        val record = mock(Record::class.java)

        given(dslContext.selectFrom(userTable)).willReturn(selectStep)
        given(selectStep.where(idField.eq(userId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(record)
        given(record.get(idField, String::class.java)).willReturn("id")
        given(record.get(emailField, String::class.java)).willReturn("email")
        given(record.get(passwordField, String::class.java)).willReturn("password")
        given(record.get(lastNameField, String::class.java)).willReturn("last")
        given(record.get(firstNameField, String::class.java)).willReturn("first")
        given(record.get(allowedField, Boolean::class.java)).willReturn(false)

        // when
        val actual = googleUserRepository.findById(userId).blockingGet()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testFindByIdFail() {
        // given
        val userId = "id"
        val selectStep = mock(SelectWhereStep::class.java) as SelectWhereStep<Record>
        val selectConditionStep = mock(SelectConditionStep::class.java) as SelectConditionStep<Record>

        given(dslContext.selectFrom(userTable)).willReturn(selectStep)
        given(selectStep.where(idField.eq(userId))).willReturn(selectConditionStep)
        given(selectConditionStep.fetchOne()).willReturn(null)

        // when
        val actual = googleUserRepository.findById(userId).blockingGet()

        // then
        assertThat(actual).isNull()
    }

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
        val actual = googleUserRepository.findByEmail(email).blockingGet()

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
        val actual = googleUserRepository.findByEmail(email).blockingGet()

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
        val actual = googleUserRepository.updateUser(user).blockingGet()

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
        val actual = googleUserRepository.ping()

        // then
        assertThat(actual).isFalse()
    }
}
