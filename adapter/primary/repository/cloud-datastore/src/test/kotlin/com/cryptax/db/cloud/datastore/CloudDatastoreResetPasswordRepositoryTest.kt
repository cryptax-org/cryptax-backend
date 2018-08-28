package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.ResetPassword
import com.google.cloud.Timestamp
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.GqlQuery
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyFactory
import com.google.cloud.datastore.Query
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime

@DisplayName("Cloud datastore transaction test")
@ExtendWith(MockitoExtension::class)
class CloudDatastoreResetPasswordRepositoryTest {

    @Mock
    lateinit var datastore: Datastore
    @InjectMocks
    lateinit var repo: CloudDatastoreResetPasswordRepository

    @Test
    fun testSave() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val date = ZonedDateTime.now()
        val resetPassword = ResetPassword("123", "token", date)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.save(resetPassword).blockingGet()

        // then
        assertThat(actual).isEqualTo(resetPassword)
        argumentCaptor<Entity>().apply {
            then(datastore).should().put(capture())
            assertThat(firstValue.key.name).isEqualTo(resetPassword.userId)
            assertThat(firstValue.getString("token")).isEqualTo("token")
            assertThat(firstValue.getTimestamp("date")).isEqualTo(Timestamp.ofTimeSecondsAndNanos(resetPassword.date.toInstant().epochSecond, resetPassword.date.toInstant().nano))
        }
    }

    @Test
    fun testFindByUserId() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val date = ZonedDateTime.now()
        val resetPassword = ResetPassword("123", "token", date)
        val entity = Entity.newBuilder(keyFactory.setKind("User").newKey(resetPassword.userId))
            .set("token", resetPassword.token)
            .set("date", Timestamp.ofTimeSecondsAndNanos(resetPassword.date.toInstant().epochSecond, resetPassword.date.toInstant().nano))
            .build()
        given(datastore.newKeyFactory()).willReturn(keyFactory)
        given(datastore.get(any<Key>())).willReturn(entity)

        // when
        val actual = repo.findByUserId(resetPassword.userId).blockingGet()

        // then
        assertThat(actual.userId).isEqualTo(resetPassword.userId)
        assertThat(actual.token).isEqualTo(resetPassword.token)
        assertThat(actual.date.toEpochSecond()).isEqualTo(resetPassword.date.toEpochSecond())
        argumentCaptor<Key>().apply {
            then(datastore).should().get(capture())
            assertThat(firstValue.name).isEqualTo(resetPassword.userId)
        }
    }

    @Test
    fun testFindByUserIdNotFound() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val date = ZonedDateTime.now()
        val resetPassword = ResetPassword("123", "token", date)
        given(datastore.newKeyFactory()).willReturn(keyFactory)
        given(datastore.get(any<Key>())).willReturn(null)

        // when
        val actual = repo.findByUserId(resetPassword.userId).blockingGet()

        // then
        assertThat(actual).isNull()
        argumentCaptor<Key>().apply {
            then(datastore).should().get(capture())
            assertThat(firstValue.name).isEqualTo(resetPassword.userId)
        }
    }

    @Test
    fun testDelete() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val userId = "123"
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.delete(userId).blockingGet()

        // then
        assertThat(actual).isEqualTo(Unit)
        argumentCaptor<Key>().apply {
            then(datastore).should().delete(capture())
            assertThat(firstValue.name).isEqualTo(userId)
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
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT token FROM ResetPassword LIMIT 1").setAllowLiteral(true).build())
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
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT token FROM ResetPassword LIMIT 1").setAllowLiteral(true).build())
        }
    }
}
