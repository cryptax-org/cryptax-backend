package com.cryptax.app

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.core.IsEqual
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

    private val appConfig = TestAppConfig()

    // FIXME: Test is still ran when web server does not deploy
    @BeforeAll
    internal fun beforeAll() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        RestAssured.port = appConfig.properties.server.port
        RestAssured.baseURI = "http://" + appConfig.properties.server.domain
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestAppConfig()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(5, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Create a user")
    fun createUser(testContext: VertxTestContext) {
        createUser()
        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user")
    fun getOneUser(testContext: VertxTestContext) {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/${token.getString("id")}").
        then().
            log().all().
            assertThat().body("id", IsEqual(token.getString("id"))).
            assertThat().body("email", IsEqual(user.email)).
            assertThat().body("password", nullValue()).
            assertThat().body("lastName", IsEqual(user.lastName)).
            assertThat().body("firstName", IsEqual(user.firstName)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user with refresh token")
    fun getOneUserWithRefreshToken(testContext: VertxTestContext) {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("refreshToken")}")).
        get("/users/${token.getString("id")}").
        then().
            log().all().
            assertThat().body("error", IsEqual("Unauthorized")).
            assertThat().statusCode(401)
        // @formatter:on

        testContext.completeNow()
    }
}
