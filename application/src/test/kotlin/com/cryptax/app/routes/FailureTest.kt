package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import io.reactivex.exceptions.CompositeException
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.ext.web.impl.RoutingContextImpl
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Failure check")
@ExtendWith(VertxExtension::class)
class FailureTest {

    private val host = "localhost"
    private val port = 8282
    private lateinit var router: Router

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        router = Router.router(vertx)
        vertx.createHttpServer().requestHandler { router.accept(it) }.listen(port) { ar ->
            if (ar.succeeded())
                testContext.completeNow()
            else
                testContext.failNow(AssertionError("Something went wrong"))
        }
        testContext.awaitCompletion(5, TimeUnit.SECONDS)
    }

    @DisplayName("401 test")
    @Test
    fun test401(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler { context -> (context as RoutingContextImpl).fail(401) }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(401)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isEqualTo("Unauthorized")
                        testContext.completeNow()
                    }
                }
            }
        }
    }

    @DisplayName("Validation exception test")
    @Test
    fun testValidationException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler { context -> (context as RoutingContextImpl).fail(ValidationException("")) }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation login exception test")
    @Test
    fun testLoginException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(LoginException("email", "desc"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(401)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation user not found exception test")
    @Test
    fun testUserNotFoundException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserNotFoundException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation user already exists exception test")
    @Test
    fun testUserAlreadyExistsException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserAlreadyExistsException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation user validation exception test")
    @Test
    fun testUserValidationException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(UserValidationException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isEqualTo("id")
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation not handled exception test")
    @Test
    fun testNotHandledException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(RuntimeException())
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(500)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isEqualTo("Something went wrong")
                        testContext.completeNow()
                    }
                }
            }
        }
    }

    @DisplayName("Validation transaction exception test")
    @Test
    fun testNotHandledTransactionException(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(TransactionValidationException("id"))
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation composite exception test")
    @Test
    fun testNotHandledCompositeException(vertx: Vertx, testContext: VertxTestContext) {
        val compositeException = CompositeException(RuntimeException())
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(compositeException)
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(500)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isNotNull()
                    }
                    testContext.completeNow()
                }
            }
        }
    }

    @DisplayName("Validation composite/cryptax exception test")
    @Test
    fun testNotHandledCompositeCryptaxException(vertx: Vertx, testContext: VertxTestContext) {
        val compositeException = CompositeException(UserNotFoundException("id"))
        router.route()
            .handler {
                (it as RoutingContextImpl).fail(compositeException)
            }
            .failureHandler(failureHandler)

        vertx.createHttpClient().getNow(port, host, "/") { resp ->
            testContext.verify {
                assertThat(resp.statusCode()).isEqualTo(400)
                resp.bodyHandler {
                    val body = JsonObject(it)
                    testContext.verify {
                        assertThat(body.getString("error")).isEqualTo("Bad request")
                    }
                    testContext.completeNow()
                }
            }
        }
    }
}
