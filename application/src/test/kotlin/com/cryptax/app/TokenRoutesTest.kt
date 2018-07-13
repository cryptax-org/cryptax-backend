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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
@DisplayName("Integration tests on basic flow")
class TokenRoutesTest {

    lateinit var vertx: Vertx

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
    @DisplayName("Get a token")
    fun getToken(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", user.password.joinToString(""))
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig())) { asyncResult ->
            if (asyncResult.succeeded()) {
                client.post(8080, "localhost", "/users").sendJson(JsonObject.mapFrom(user)) { ar1 ->
                    if (ar1.succeeded()) {
                        client.post(8080, "localhost", "/token").sendJson(token) { ar2 ->
                            // then
                            testContext.verify {
                                assert(ar2.succeeded()) { "Something went wrong while handling the request" }
                                assertEquals(200, ar2.result().statusCode()) { "Wrong status in the response" }
                                val body = ar2.result().bodyAsJsonObject()
                                assertNotNull(body.getString("id")) { "id is null" }
                                assertNotNull(body.getString("token")) { "token is null" }
                            }
                            testContext.completeNow()
                        }
                    } else {
                        testContext.failNow(ar1.cause())
                    }
                }
            } else {
                testContext.failNow(asyncResult.cause())
            }
        }
    }

    @Test
    @DisplayName("Get a token with wrong password")
    fun getTokenWrongPassword(testContext: VertxTestContext) {
        // given
        val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
        val token = JsonObject().put("email", user.email).put("password", "wrong password")
        val client = WebClient.create(vertx)

        // when
        vertx.deployVerticle(RestApplication(DefaultConfig())) { asyncResult ->
            if (asyncResult.succeeded()) {
                client.post(8080, "localhost", "/users").sendJson(JsonObject.mapFrom(user)) { ar1 ->
                    if (ar1.succeeded()) {
                        client.post(8080, "localhost", "/token").sendJson(token) { ar2 ->
                            // then
                            testContext.verify {
                                assert(ar2.succeeded()) { "Something went wrong while handling the request" }
                                assertEquals(401, ar2.result().statusCode()) { "Wrong status in the response" }
                                val body = ar2.result().bodyAsJsonObject()
                                assertNotNull(body.getString("error")) { "id is null" }
                            }
                            testContext.completeNow()
                        }
                    } else {
                        testContext.failNow(ar1.cause())
                    }
                }
            } else {
                testContext.failNow(asyncResult.cause())
            }
        }
    }
}
