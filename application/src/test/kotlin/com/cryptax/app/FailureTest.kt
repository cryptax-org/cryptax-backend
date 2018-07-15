package com.cryptax.app

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.ext.web.impl.RoutingContextImpl
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Failure check")
@ExtendWith(VertxExtension::class)
class FailureTest {

    private val host = "localhost"
    private val port = 8282

    @DisplayName("401 test")
    @Test
    fun test401(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(401)
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(401, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertEquals("Unauthorized", body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation exception test")
    @Test
    fun testValidationException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(ValidationException(""))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(400, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertNotNull(body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation login exception test")
    @Test
    fun testLoginException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(LoginException("email", "desc"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(401, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertNotNull(body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation user not found exception test")
    @Test
    fun testUserNotFoundException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserNotFoundException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(400, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertNotNull(body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation user already exists exception test")
    @Test
    fun testUserAlreadyExistsException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserAlreadyExistsException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(400, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertNotNull(body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation user validation exception test")
    @Test
    fun testUserValidationException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserValidationException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(400, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertEquals("id", body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("Validation not handled exception test")
    @Test
    fun testNotHandledException(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(RuntimeException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/") { resp ->
                        // then
                        testContext.verify {
                            assertEquals(500, resp.statusCode())
                            resp.bodyHandler {
                                val body = JsonObject(it)
                                assertEquals("id", body.getString("error"))
                            }
                            testContext.completeNow()
                        }
                    }
                } else {
                    fail("The server did not start")
                }
            }
    }
}
