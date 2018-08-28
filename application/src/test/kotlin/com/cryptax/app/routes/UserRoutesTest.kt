package com.cryptax.app.routes

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.config.kodein
import com.cryptax.app.createUser
import com.cryptax.app.initUserAndGetToken
import com.cryptax.app.setupRestAssured
import com.cryptax.app.user
import com.cryptax.app.verticle.RestVerticle
import com.nhaarman.mockitokotlin2.isNotNull
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsNull.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("User routes integration tests")
class UserRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured()
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestAppConfig(), kodein()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Create a user")
    fun testCreateUser(testContext: VertxTestContext) {
        createUser()
        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user")
    fun testGetOneUser(testContext: VertxTestContext) {
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
            assertThat().body("id", equalTo(token.getString("id"))).
            assertThat().body("email", equalTo(user.email)).
            assertThat().body("password", nullValue()).
            assertThat().body("lastName", equalTo(user.lastName)).
            assertThat().body("firstName", equalTo(user.firstName)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user with refresh token")
    fun testGetOneUserWithRefreshToken(testContext: VertxTestContext) {
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
            assertThat().body("error", equalTo("Unauthorized")).
            assertThat().statusCode(401)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Send welcome email")
    fun testSendWelcomeEmail(testContext: VertxTestContext) {
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

        testContext.completeNow()
    }

    @Test
    @DisplayName("Send welcome email, user not found")
    fun testSendWelcomeEmailUserNotFound(testContext: VertxTestContext) {
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

        testContext.completeNow()
    }

    @Test
    @DisplayName("Initiate reset password")
    fun testInitiatePasswordReset(testContext: VertxTestContext) {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/${pair.first.email}/reset").
        then().
            log().ifValidationFails().
            assertThat().body("token", notNullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Initiate reset password, user not found")
    fun testInitiatePasswordResetNotFound(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/email@email.com/reset").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on

        testContext.completeNow()
    }
}
