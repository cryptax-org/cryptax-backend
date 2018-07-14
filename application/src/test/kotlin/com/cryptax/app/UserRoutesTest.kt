package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.config.DefaultConfig
import com.cryptax.domain.entity.User
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("user routes integration tests")
class UserRoutesTest {

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
    @DisplayName("Create a user")
    fun createUser(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        createUser(user)
        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user")
    fun getOneUser(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)
        val result = getToken(credentials)

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${result.getString("token")}")).
        get("/users/${result.getString("id")}").
        then().
            log().all().
            assertThat().body("id", IsEqual(result.getString("id"))).
            assertThat().body("email", IsEqual(user.email)).
            assertThat().body("password", IsNull.nullValue()).
            assertThat().body("lastName", IsEqual(user.lastName)).
            assertThat().body("firstName", IsEqual(user.firstName)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get all users")
    fun getAllUsers(testContext: VertxTestContext) {
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)
        val result = getToken(credentials)

        // @formatter:off
        given().
           log().all().
           contentType(ContentType.JSON).
           header(Header("Authorization", "Bearer ${result.getString("token")}")).
        get("/users").
        then().
            log().all().
            assertThat().body("[0].id", IsEqual(result.getString("id"))).
            assertThat().body("[0].email", IsEqual(user.email)).
            assertThat().body("[0].password", IsNull.nullValue()).
            assertThat().body("[0].lastName", IsEqual(user.lastName)).
            assertThat().body("[0].firstName", IsEqual(user.firstName)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one user with refresh token")
    fun getOneUserWithRefreshToken(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()
        createUser(user)
        val result = getToken(credentials)

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${result.getString("refreshToken")}")).
        get("/users/${result.getString("id")}").
        then().
            log().all().
            assertThat().body("error", IsEqual("Unauthorized")).
            assertThat().statusCode(401)
        // @formatter:on

        testContext.completeNow()
    }
}
