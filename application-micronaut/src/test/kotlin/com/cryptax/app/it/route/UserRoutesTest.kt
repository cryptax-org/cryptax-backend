package com.cryptax.app.it.route

import com.cryptax.app.Application
import com.cryptax.app.it.route.Utils.createUser
import com.cryptax.app.it.route.Utils.initUserAndGetToken
import com.cryptax.app.it.route.Utils.setupRestAssured
import com.cryptax.app.it.route.Utils.user
import com.cryptax.app.model.ResetPasswordRequest
/*import com.cryptax.app.route.Utils.createUser
import com.cryptax.app.route.Utils.initUserAndGetToken
import com.cryptax.app.route.Utils.initiatePasswordReset
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.app.route.Utils.user*/
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.UserRepository
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

@DisplayName("User routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class UserRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    lateinit var memory: UserRepository

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(server.port)
    }

    @AfterAll
    internal fun `after all`() {
        (memory as InMemoryUserRepository).deleteAll()
    }

    @BeforeEach
    fun setUp() {
        (memory as InMemoryUserRepository).deleteAll()
    }

    @DisplayName("Create a user")
    @Test
    fun `create a user`() {
        createUser()
    }

    @DisplayName("Create a user, no body")
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

    @DisplayName("Create a user, exists already")
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

    @DisplayName("Allow user")
    @Test
    fun testAllowUser() {
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

    @DisplayName("Allow user, missing token")
    @Test
    fun testAllowUserNoToken() {
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

    @DisplayName("Allow user, missing token")
    @Test
    fun testAllowUserWrongToken() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow?token=WrongToken").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }

    @DisplayName("Get one user")
    @Test
    fun testGetOneUser() {
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

    @DisplayName("Get one user, user do not exists")
    @Test
    fun `get one user, user does not exits`() {
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
            assertThat().statusCode(401)
        // @formatter:on
    }
/*
    @DisplayName("Get one user with refresh token")
    @Test
    fun testGetOneUserWithRefreshToken() {
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
            assertThat().statusCode(401)
        // @formatter:on
    }

    @DisplayName("Send welcome email")
    @Test
    fun testSendWelcomeEmail() {
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

    @DisplayName("Send welcome email, user not found")
    @Test
    fun testSendWelcomeEmailUserNotFound() {
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

    @DisplayName("Initiate reset password")
    @Test
    fun `initiate reset password`() {
        // given
        val pair = createUser()

        initiatePasswordReset(pair)
    }

    @DisplayName("Initiate reset password, user not found")
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

    @DisplayName("Reset password")
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

    @DisplayName("Reset password, param error")
    @Test
    fun `reset password, param error`() {
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

    @DisplayName("Reset password, unknown user")
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
    }*/
}
