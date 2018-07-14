package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.config.DefaultConfig
import com.cryptax.domain.entity.User
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Token routes integration tests")
class TokenRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        RestAssured.port = Config.config.server.port
        RestAssured.baseURI = "http://" + Config.config.server.domain
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestApplication(DefaultConfig()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(5, TimeUnit.SECONDS)
        // Ugly fix to ensure the server is started
        // Even if the call back is called the server seems not ready
        Thread.sleep(100)
    }

    @Test
    @DisplayName("Get a token")
    fun getToken(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)

        // @formatter:off
        given().
            log().all().
            body(token).
            contentType(ContentType.JSON).
        post("/token").
        then().
            log().all().
            assertThat().body("token", notNullValue()).
            assertThat().body("refreshToken", notNullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get a token with wrong password")
    fun getTokenWrongPassword(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", "wrong password").toString()
        createUser(user)

        // @formatter:off
        given().
            log().all().
            body(token).
            header(Header("Content-Type", "application/json")).
        post("/token").
        then().
            log().all().
            assertThat().body("error", IsEqual("Unauthorized")).
            assertThat().statusCode(401)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get a refresh token")
    fun getTokenRefreshToken(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)
        val result = getToken(credentials)

        // @formatter:off
        given().
            log().all().
            body(credentials).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${result.getString("refreshToken")}")).
        get("/refresh").
        then().
            log().all().
            assertThat().body("token", notNullValue()).
            assertThat().body("refreshToken", notNullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get a refresh token with wrong token")
    fun getTokenRefreshTokenWithWrongToken(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)
        val result = getToken(credentials)

        // @formatter:off
        given().
            log().all().
            body(credentials).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${result.getString("token")}")).
        get("/refresh").
        then().
            log().all().
            assertThat().body("error", IsEqual("Unauthorized")).
            assertThat().statusCode(401)
        // @formatter:on

        testContext.completeNow()
    }
}
