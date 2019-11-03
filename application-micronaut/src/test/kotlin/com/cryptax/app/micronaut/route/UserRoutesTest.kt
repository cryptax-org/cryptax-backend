package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.Utils
import com.cryptax.app.micronaut.Utils.createUser
import com.cryptax.app.micronaut.Utils.initUserAndGetToken
import com.cryptax.app.micronaut.Utils.initiatePasswordReset
import com.cryptax.app.micronaut.Utils.user
import com.cryptax.app.micronaut.model.ResetPasswordRequest
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.UserRepository
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class UserRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    lateinit var memory: UserRepository

    @BeforeAll
    fun beforeAll() {
        Utils.setupRestAssured(server.port)
    }

    @BeforeEach
    fun setUp() {
        (memory as InMemoryUserRepository).deleteAll()
    }

    @Test
    fun `create a user`() {
        createUser()
    }

    @Test
    fun `create a user with empty body`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            body("{}").
            contentType(ContentType.JSON).
        post("/users").
        then().
        log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details", hasItems(
                                                        "Email can not be empty",
                                                        "Password can not be empty",
                                                        "Last name can not be empty",
                                                        "First name can not be empty"))
        // @formatter:on
    }

    @Test
    fun `create a user, but it exists already`() {
        createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(user).
            contentType(ContentType.JSON).
        post("/users").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @Test
    fun `allow a user`() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow?token=${pair.second}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @Test
    fun `allow a user with no token`() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details[0]", equalTo("Required QueryValue [token] not specified"))
        // @formatter:on
    }

    @Test
    fun `allow a user with a wrong token`() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow?token=WrongToken").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @Test
    fun `get one user`() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/${token.getString("id")}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("id", equalTo(token.getString("id"))).
            assertThat().body("email", equalTo(user.email)).
            assertThat().body("password", nullValue()).
            assertThat().body("lastName", equalTo(user.lastName)).
            assertThat().body("firstName",equalTo(user.firstName))
        // @formatter:on
    }

    @Test
    fun `get one user that does not exists`() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/WroNgId").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401).
            assertThat().body(isEmptyOrNullString())
        // @formatter:on
    }

    @Test
    fun `get one user with refresh token`() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("refreshToken")}")).
        get("/users/${token.getString("id")}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401).
            assertThat().body(isEmptyOrNullString())
        // @formatter:on
    }

    @Test
    fun `send welcome email`() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/${pair.first.email}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @Test
    fun `send welcome email user not found`() {
        // given
        val email = "whatever"

        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/$email").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @Test
    fun `initiate reset password`() {
        // given
        val pair = createUser()

        initiatePasswordReset(pair)
    }

    @Test
    fun `initiate reset password, user not found`() {
        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/email@email.com/reset").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @Test
    fun `reset password`() {
        val pair = createUser()
        val token = initiatePasswordReset(pair).getString("token")

        val resetPassword = ResetPasswordRequest(pair.first.email, "pass".toCharArray(), token)

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            body(resetPassword).
            log().ifValidationFails().
        put("/users/password").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @Test
    fun `reset password, validation error`() {
        // @formatter:off
        given().
            contentType(ContentType.JSON).
            body("{}").
            log().ifValidationFails().
        put("/users/password").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details", hasItems(
                                                        "Email can not be empty",
                                                        "Password can not be empty",
                                                        "Token can not be empty"))
        // @formatter:on
    }

    @Test
    fun `reset password, unknown user`() {
        val resetPassword = ResetPasswordRequest("email@email.com", "dwqdqd".toCharArray(), "token")

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            body(resetPassword).
            log().ifValidationFails().
        put("/users/password").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }
}
