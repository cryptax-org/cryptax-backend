package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.config.DefaultConfig
import com.cryptax.domain.entity.User
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
@DisplayName("Integration tests on basic flow")
class UserRoutesTest {

    private val port = 8080
    private val domain = "localhost"
    private lateinit var vertx: Vertx

    @BeforeEach
    fun beforeEach() {
        vertx = Vertx.vertx(VertxOptions()
            .setMaxEventLoopExecuteTime(1000)
            .setPreferNativeTransport(true)
            .setFileResolverCachingEnabled(true))
    }

    @AfterEach
    fun afterEach() {
        vertx.close()
    }

    @Test
    @DisplayName("Create a user")
    fun createUser(testContext: VertxTestContext) {
        /*// given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig())) { asyncResult ->
            if (asyncResult.succeeded()) {
                client.post(port, domain, "/users").sendJson(JsonObject.mapFrom(user)) { ar2 ->
                    // then
                    testContext.verify {
                        assert(ar2.succeeded()) { "Something went wrong while handling the request" }
                        assertEquals(200, ar2.result().statusCode()) { "Wrong status in the response" }
                        val body = ar2.result().bodyAsJsonObject()
                        assertNotNull(body.getString("id")) { "id is null" }
                        assertEquals(user.email, body.getString("email")) { "email do not match" }
                        assertNull(body.getString("password")) { "password do not match" }
                        assertEquals(user.lastName, body.getString("lastName")) { "lastName do not match" }
                        assertEquals(user.firstName, body.getString("firstName")) { "firstName do not match" }
                    }
                    testContext.completeNow()
                }
            } else {
                testContext.failNow(asyncResult.cause())
            }
        }*/
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig()), testContext.succeeding {
            client.post(port, domain, "/users").sendJson(JsonObject.mapFrom(user), testContext.succeeding { response ->
                testContext.verify {
                    val body = response.bodyAsJsonObject()
                    assertNotNull(body.getString("id")) { "id is null" }
                    assertEquals(user.email, body.getString("email")) { "email do not match" }
                    assertNull(body.getString("password")) { "password do not match" }
                    assertEquals(user.lastName, body.getString("lastName")) { "lastName do not match" }
                    assertEquals(user.firstName, body.getString("firstName")) { "firstName do not match" }
                    testContext.completeNow()
                }
            })
        })
    }

    @Test
    @DisplayName("Get one user")
    fun getOneUser(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", user.password.joinToString(""))
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig())) { asyncResult ->
            if (asyncResult.succeeded()) {
                // Create a user
                client.post(port, domain, "/users").sendJson(JsonObject.mapFrom(user)) { ar ->
                    if (ar.succeeded()) {
                        testContext.verify {
                            assertEquals(200, ar.result().statusCode()) { "Wrong status in the response" }
                        }
                        val userId = ar.result().bodyAsJsonObject().getString("id", "idNotFound")
                        // Get its token
                        client.post(port, domain, "/token").sendJson(token) { ar2 ->
                            testContext.verify {
                                assert(ar2.succeeded()) { "Something went wrong while handling the request" }
                                assertEquals(200, ar2.result().statusCode()) { "Wrong status in the response" }
                            }
                            val tokenValue = ar2.result().bodyAsJsonObject().getString("token", "tokenNotFound")
                            client.get(port, domain, "/users/$userId").putHeader("Authorization", "Bearer $tokenValue").send { ar3 ->
                                // then
                                testContext.verify {
                                    assert(ar3.succeeded()) { "Something went wrong while handling the request" }
                                    assertEquals(200, ar3.result().statusCode()) { "Wrong status in the response" }
                                    val body = ar3.result().bodyAsJsonObject()
                                    assertThat(body.getString("id")).isEqualTo(userId)
                                    testContext.completeNow()
                                }
                            }
                        }
                    } else {
                        testContext.failNow(ar.cause())
                    }
                }
            } else {
                testContext.failNow(asyncResult.cause())
            }
        }
    }

    @Test
    @DisplayName("Get all users")
    fun getAllUsers(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", user.password.joinToString(""))
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig())) { asyncResult ->
            if (asyncResult.succeeded()) {
                // Create a user
                client.post(port, domain, "/users").sendJson(JsonObject.mapFrom(user)) { ar ->
                    if (ar.succeeded()) {
                        testContext.verify {
                            assert(ar.succeeded()) { "Something went wrong while handling the request" }
                            assertEquals(200, ar.result().statusCode()) { "Wrong status in the response" }
                        }
                        val userId = ar.result().bodyAsJsonObject().getString("id", "idNotFound")
                        // Get its token
                        client.post(port, domain, "/token").sendJson(token) { ar2 ->
                            testContext.verify {
                                assert(ar2.succeeded()) { "Something went wrong while handling the request" }
                                assertEquals(200, ar2.result().statusCode()) { "Wrong status in the response" }
                            }
                            val tokenValue = ar2.result().bodyAsJsonObject().getString("token", "tokenNotFound")
                            client.get(port, domain, "/users").putHeader("Authorization", "Bearer $tokenValue").send { ar3 ->
                                // then
                                testContext.verify {
                                    assert(ar3.succeeded()) { "Something went wrong while handling the request" }
                                    assertEquals(200, ar3.result().statusCode()) { "Wrong status in the response" }
                                    val body = ar3.result().bodyAsJsonArray()
                                    assertThat(body).hasSize(1)
                                    assertThat(body.getJsonObject(0).getString("id")).isEqualTo(userId)
                                    testContext.completeNow()
                                }
                            }
                        }
                    } else {
                        testContext.failNow(ar.cause())
                    }
                }
            } else {
                testContext.failNow(asyncResult.cause())
            }
        }
    }
}
