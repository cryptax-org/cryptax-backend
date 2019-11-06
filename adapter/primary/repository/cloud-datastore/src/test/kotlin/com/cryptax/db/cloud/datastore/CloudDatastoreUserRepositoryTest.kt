package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.User
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Cloud datastore transaction test")
@ExtendWith(MockitoExtension::class)
class CloudDatastoreUserRepositoryTest {

    @Mock
    lateinit var queryResults: QueryResults<Entity>
    @Mock
    lateinit var datastore: Datastore
    @InjectMocks
    lateinit var repo: CloudDatastoreUserRepository

    @Test
    fun `add`() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.create(user).blockingGet()

        // then
        assertThat(actual).isEqualTo(user)
        then(datastore).should().newKeyFactory()
        argumentCaptor<Entity>().apply {
            then(datastore).should().put(capture())
            assertThat(firstValue.key.name).isEqualTo(user.id)
            assertThat(firstValue.getString("email")).isEqualTo(user.email)
            assertThat(firstValue.getString("password")).isEqualTo(user.password.joinToString(""))
            assertThat(firstValue.getString("lastName")).isEqualTo(user.lastName)
            assertThat(firstValue.getString("firstName")).isEqualTo(user.firstName)
            assertThat(firstValue.getBoolean("allowed")).isEqualTo(user.allowed)
        }
    }

    @Test
    fun `find by id`() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        val entity = Entity.newBuilder(keyFactory.setKind("User").newKey(user.id))
            .set("email", user.email)
            .set("password", user.password.joinToString(""))
            .set("lastName", user.lastName)
            .set("firstName", user.firstName)
            .set("allowed", user.allowed)
            .build()
        given(datastore.newKeyFactory()).willReturn(keyFactory)
        given(datastore.get(any<Key>())).willReturn(entity)

        // when
        val actual = repo.findById(user.id).blockingGet()

        // then
        then(datastore).should().newKeyFactory()
        then(datastore).should().get(any<Key>())
        assertThat(actual).isEqualTo(user)
    }

    @Test
    fun `get, not found`() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        given(datastore.newKeyFactory()).willReturn(keyFactory)
        given(datastore.get(any<Key>())).willReturn(null)

        // when
        val actual = repo.findById(user.id).blockingGet()

        // then
        then(datastore).should().newKeyFactory()
        then(datastore).should().get(any<Key>())
        assertThat(actual).isNull()
    }

    @Test
    fun `find by email`() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        val entity = Entity.newBuilder(keyFactory.setKind("User").newKey(user.id))
            .set("email", user.email)
            .set("password", user.password.joinToString(""))
            .set("lastName", user.lastName)
            .set("firstName", user.firstName)
            .set("allowed", user.allowed)
            .build()
        given(datastore.run(any<EntityQuery>())).willReturn(queryResults)
        given(queryResults.hasNext()).willReturn(true, false)
        given(queryResults.next()).willReturn(entity)

        // when
        val actual = repo.findByEmail(user.email).blockingGet()

        // then
        assertThat(actual).isEqualTo(user)
        then(queryResults).should().next()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newEntityQueryBuilder()
                .setKind("User")
                .setFilter(StructuredQuery.PropertyFilter.eq("email", user.email))
                .build())
        }
    }

    @Test
    fun `find by email, not found`() {
        // given
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        given(datastore.run(any<EntityQuery>())).willReturn(queryResults)
        given(queryResults.hasNext()).willReturn(false)

        // when
        val actual = repo.findByEmail(user.email).blockingGet()

        // then
        assertThat(actual).isNull()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newEntityQueryBuilder()
                .setKind("User")
                .setFilter(StructuredQuery.PropertyFilter.eq("email", user.email))
                .build())
        }
    }

    @Test
    fun `update`() {
        // given
        val keyFactory = KeyFactory("cryptax-212416")
        val user = User("1", "john.doe@proton.com", "".toCharArray(), "Doe", "John", true)
        given(datastore.newKeyFactory()).willReturn(keyFactory)

        // when
        val actual = repo.updateUser(user).blockingGet()

        // then
        assertThat(actual).isEqualTo(user)
        then(datastore).should().newKeyFactory()
        argumentCaptor<Entity>().apply {
            then(datastore).should().update(capture())
            assertThat(firstValue.key.name).isEqualTo(user.id)
            assertThat(firstValue.getString("email")).isEqualTo(user.email)
            assertThat(firstValue.getString("password")).isEqualTo(user.password.joinToString(""))
            assertThat(firstValue.getString("lastName")).isEqualTo(user.lastName)
            assertThat(firstValue.getString("firstName")).isEqualTo(user.firstName)
            assertThat(firstValue.getBoolean("allowed")).isEqualTo(user.allowed)
        }
    }

    @Test
    fun `ping`() {
        // when
        val actual = repo.ping()

        // then
        assertThat(actual).isTrue()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT email FROM User LIMIT 1").setAllowLiteral(true).build())
        }
    }

    @Test
    fun `ping, fails`() {
        // given
        given(datastore.run(any<GqlQuery<Boolean>>())).willThrow(DatastoreException(0, "", ""))

        // when
        val actual = repo.ping()

        // then
        assertThat(actual).isFalse()
        argumentCaptor<Query<Entity>>().apply {
            then(datastore).should().run(capture())
            assertThat(firstValue).isEqualTo(Query.newGqlQueryBuilder("SELECT email FROM User LIMIT 1").setAllowLiteral(true).build())
        }
    }
}
