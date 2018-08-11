package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.google.cloud.Timestamp
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.EntityQuery
import com.google.cloud.datastore.GqlQuery
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyFactory
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StructuredQuery
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.then
import com.nhaarman.mockitokotlin2.times
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZoneId
import java.time.ZonedDateTime

@DisplayName("Cloud datastore transaction test")
@ExtendWith(MockitoExtension::class)
class CloudDatastoreTransactionRepositoryTest {

    @Mock
    lateinit var queryResults: QueryResults<Entity>
    @Mock
    lateinit var datastore: Datastore
    @InjectMocks
    lateinit var repo: CloudDatastoreTransactionRepository

    @Test
    fun testAdd() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val transaction = Transaction(
            id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.add(transaction).blockingGet()

        // then
        assertThat(actual).isEqualTo(transaction)
        then(datastore).should().newKeyFactory()
        argumentCaptor<Entity>().apply {
            then(datastore).should().put(capture())
            assertThat(firstValue.key.name).isEqualTo(transaction.id)
            assertThat(firstValue.getString("userId")).isEqualTo(transaction.userId)
            assertThat(firstValue.getString("source")).isEqualTo(transaction.source.name)
            assertThat(firstValue.getTimestamp("date")).isEqualTo(Timestamp.ofTimeSecondsAndNanos(transaction.date.toInstant().epochSecond, transaction.date.toInstant().nano))
            assertThat(firstValue.getString("type")).isEqualTo(transaction.type.name)
            assertThat(firstValue.getDouble("price")).isEqualTo(transaction.price)
            assertThat(firstValue.getDouble("quantity")).isEqualTo(transaction.quantity)
            assertThat(firstValue.getString("currency1")).isEqualTo(transaction.currency1.code)
            assertThat(firstValue.getString("currency2")).isEqualTo(transaction.currency2.code)
        }
    }

    @Test
    fun testAddList() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val transaction1 = Transaction(
            id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        val transaction2 = Transaction(
            id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.add(listOf(transaction1, transaction2)).blockingGet()

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0]).isEqualTo(transaction1)
        assertThat(actual[1]).isEqualTo(transaction2)
        then(datastore).should(times(2)).newKeyFactory()
        argumentCaptor<Entity>().apply {
            then(datastore).should().put(capture(), capture())
            assertThat(firstValue.key.name).isEqualTo(transaction1.id)
            assertThat(firstValue.getString("userId")).isEqualTo(transaction1.userId)
            assertThat(firstValue.getString("source")).isEqualTo(transaction1.source.name)
            assertThat(firstValue.getTimestamp("date")).isEqualTo(Timestamp.ofTimeSecondsAndNanos(transaction1.date.toInstant().epochSecond, transaction1.date.toInstant().nano))
            assertThat(firstValue.getString("type")).isEqualTo(transaction1.type.name)
            assertThat(firstValue.getDouble("price")).isEqualTo(transaction1.price)
            assertThat(firstValue.getDouble("quantity")).isEqualTo(transaction1.quantity)
            assertThat(firstValue.getString("currency1")).isEqualTo(transaction1.currency1.code)
            assertThat(firstValue.getString("currency2")).isEqualTo(transaction1.currency2.code)

            assertThat(secondValue.key.name).isEqualTo(transaction1.id)
            assertThat(secondValue.getString("userId")).isEqualTo(transaction2.userId)
            assertThat(secondValue.getString("source")).isEqualTo(transaction2.source.name)
            assertThat(secondValue.getTimestamp("date")).isEqualTo(Timestamp.ofTimeSecondsAndNanos(transaction2.date.toInstant().epochSecond, transaction2.date.toInstant().nano))
            assertThat(secondValue.getString("type")).isEqualTo(transaction2.type.name)
            assertThat(secondValue.getDouble("price")).isEqualTo(transaction2.price)
            assertThat(secondValue.getDouble("quantity")).isEqualTo(transaction2.quantity)
            assertThat(secondValue.getString("currency1")).isEqualTo(transaction2.currency1.code)
            assertThat(secondValue.getString("currency2")).isEqualTo(transaction2.currency2.code)
        }
    }

    @Test
    fun testGet() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val transactionId = "id"
        val transaction = Transaction(id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(ZoneId.of("UTC")),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        val entity = Entity.newBuilder(keyFactory.setKind("Transaction").newKey(transactionId))
            .set("userId", transaction.userId)
            .set("source", transaction.source.name)
            .set("date", Timestamp.ofTimeSecondsAndNanos(transaction.date.toInstant().epochSecond, transaction.date.toInstant().nano))
            .set("type", transaction.type.name)
            .set("price", transaction.price)
            .set("quantity", transaction.quantity)
            .set("currency1", transaction.currency1.code)
            .set("currency2", transaction.currency2.code)
            .build()
        given(datastore.newKeyFactory()).willReturn(keyFactory)
        given(datastore.get(any<Key>())).willReturn(entity)

        // when
        val actual = repo.get(transactionId).blockingGet()

        // then
        then(datastore).should().newKeyFactory()
        then(datastore).should().get(any<Key>())
        assertThat(actual.id).isEqualTo(transaction.id)
        assertThat(actual.userId).isEqualTo(transaction.userId)
        assertThat(actual.source).isEqualTo(transaction.source)
        assertThat(actual.date.toEpochSecond()).isEqualTo(transaction.date.toEpochSecond())
        assertThat(actual.type).isEqualTo(transaction.type)
        assertThat(actual.price).isEqualTo(transaction.price)
        assertThat(actual.quantity).isEqualTo(transaction.quantity)
        assertThat(actual.currency1).isEqualTo(transaction.currency1)
        assertThat(actual.currency2).isEqualTo(transaction.currency2)
    }

    @Test
    fun testGetAllForUser() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val transactionId = "id"
        val transaction = Transaction(id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(ZoneId.of("UTC")),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        val entity = Entity.newBuilder(keyFactory.setKind("Transaction").newKey(transactionId))
            .set("userId", transaction.userId)
            .set("source", transaction.source.name)
            .set("date", Timestamp.ofTimeSecondsAndNanos(transaction.date.toInstant().epochSecond, transaction.date.toInstant().nano))
            .set("type", transaction.type.name)
            .set("price", transaction.price)
            .set("quantity", transaction.quantity)
            .set("currency1", transaction.currency1.code)
            .set("currency2", transaction.currency2.code)
            .build()
        given(datastore.run(any<EntityQuery>())).willReturn(queryResults)
        given(queryResults.hasNext()).willReturn(true, false)
        given(queryResults.next()).willReturn(entity)

        // when
        val actual = repo.getAllForUser(transaction.userId).blockingGet()

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0].id).isEqualTo(transaction.id)
        assertThat(actual[0].userId).isEqualTo(transaction.userId)
        assertThat(actual[0].source).isEqualTo(transaction.source)
        assertThat(actual[0].date.toEpochSecond()).isEqualTo(transaction.date.toEpochSecond())
        assertThat(actual[0].type).isEqualTo(transaction.type)
        assertThat(actual[0].price).isEqualTo(transaction.price)
        assertThat(actual[0].quantity).isEqualTo(transaction.quantity)
        assertThat(actual[0].currency1).isEqualTo(transaction.currency1)
        assertThat(actual[0].currency2).isEqualTo(transaction.currency2)
        then(queryResults).should().next()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newEntityQueryBuilder()
                .setKind("Transaction")
                .setFilter(StructuredQuery.PropertyFilter.eq("userId", transaction.userId))
                .build())
        }
    }

    @Test
    fun testUpdate() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val transaction = Transaction(
            id = "id",
            userId = "userId",
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.BUY,
            price = 10.0,
            quantity = 4.0,
            currency1 = Currency.ETH,
            currency2 = Currency.BTC)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.update(transaction).blockingGet()

        // then
        assertThat(actual).isEqualTo(transaction)
        then(datastore).should().newKeyFactory()
        argumentCaptor<Entity>().apply {
            then(datastore).should().update(capture())
            assertThat(firstValue.key.name).isEqualTo(transaction.id)
            assertThat(firstValue.getString("userId")).isEqualTo(transaction.userId)
            assertThat(firstValue.getString("source")).isEqualTo(transaction.source.name)
            assertThat(firstValue.getTimestamp("date")).isEqualTo(Timestamp.ofTimeSecondsAndNanos(transaction.date.toInstant().epochSecond, transaction.date.toInstant().nano))
            assertThat(firstValue.getString("type")).isEqualTo(transaction.type.name)
            assertThat(firstValue.getDouble("price")).isEqualTo(transaction.price)
            assertThat(firstValue.getDouble("quantity")).isEqualTo(transaction.quantity)
            assertThat(firstValue.getString("currency1")).isEqualTo(transaction.currency1.code)
            assertThat(firstValue.getString("currency2")).isEqualTo(transaction.currency2.code)
        }
    }

    @Test
    fun testPing() {
        // when
        val actual = repo.ping()

        // then
        assertThat(actual).isTrue()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT userId FROM Transaction LIMIT 1").setAllowLiteral(true).build())
        }
    }

    @Test
    fun testPingFail() {
        // given
        given(datastore.run(any<GqlQuery<Boolean>>())).willThrow(DatastoreException(0, "", ""))

        // when
        val actual = repo.ping()

        // then
        assertThat(actual).isFalse()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT userId FROM Transaction LIMIT 1").setAllowLiteral(true).build())
        }
    }
}
