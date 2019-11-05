package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.Utils.createUser
import com.cryptax.app.micronaut.Utils.getToken
import com.cryptax.app.micronaut.Utils.initUserAndGetToken
import com.cryptax.app.micronaut.Utils.setupRestAssured
import com.cryptax.app.micronaut.Utils.validateToken
import com.cryptax.app.micronaut.Utils.validateUser
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.UserRepository
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class TokenRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    lateinit var userRepository: UserRepository

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(server.port)
    }

    @AfterAll
    internal fun `after all`() {
        (userRepository as InMemoryUserRepository).deleteAll()
    }

    @BeforeEach
    internal fun `before each`() {
        (userRepository as InMemoryUserRepository).deleteAll()
    }

    @Test
    fun `request a token`() {
        // given
        val pair = createUser()
        validateUser(pair)

        // when
        val token = getToken()

        // then
        val actualToken = token.getString("token")
        val actualRefreshToken = token.getString("refreshToken")
        assertThat(actualToken).isNotNull()
        assertThat(actualRefreshToken).isNotNull()
        validateToken(actualToken, pair.first.id, false)
        validateToken(actualRefreshToken, pair.first.id, true)
    }

    @Test
    fun `request a token, validation test`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            body("{}").
            contentType(ContentType.JSON).
        post("/token").
        then().
            log().ifValidationFails().
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details", hasItems(
                                                        "Email can not be empty",
                                                        "Password can not be empty"))
        // @formatter:on
    }

    @Test
    fun `request a token with wrong credentials`() {
        val pair = createUser()
        validateUser(pair)
        val credentials = JsonNodeFactory.instance.objectNode().put("email", pair.first.email).put("password", "mywrongpassword").toString()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(credentials).
            contentType(ContentType.JSON).
        post("/token").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }

    @Test
    fun `obtain token with refresh token`() {
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("refreshToken")}")).
        get("/refresh").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("token", notNullValue()).
            assertThat().body("refreshToken", notNullValue())
        // @formatter:on
    }

    @Test
    fun `request token with wrong token`() {
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/refresh").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }
}
