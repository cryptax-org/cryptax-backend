package com.cryptax.validation

import com.cryptax.domain.entity.Transaction
import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.csvContentTypeValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.loginValidation
import com.cryptax.validation.RestValidation.transactionBodyValidation
import com.cryptax.validation.RestValidation.uploadCsvValidation
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.impl.JWTUser
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

/**
 * ☢☢ Those tests are between unit test and integration tests
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Request validation validation")
@ExtendWith(VertxExtension::class)
class RestValidationTest {

    private val host = "localhost"
    private val port = 8282
    private lateinit var router: Router

    @BeforeAll
    internal fun beforeAll() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
    }

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

    @DisplayName("😀 Check create user validation")
    @Test
    fun testCreateUserValidation(vertx: Vertx, testContext: VertxTestContext) {
        val user = JsonObject().put("email", "email@email.com").put("password", "123").put("lastName", "john").put("firstName", "doe")
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check create user validation, body null")
    @Test
    fun testCreateUserValidationBodyNull(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler(createUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "Body is null or empty"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check create user validation, empty body")
    @Test
    fun testCreateUserValidationBodyEmpty(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                it.body = Buffer.buffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "Body is null or empty"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check create user validation, empty json body")
    @Test
    fun testCreateUserValidationBodyEmptyJson(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                it.body = JsonObject().toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "Body is null or empty"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check create user validation, missing password")
    @Test
    fun testCreateUserValidationMissingPassword(vertx: Vertx, testContext: VertxTestContext) {
        val user = JsonObject().put("email", "email@email.com").put("lastName", "john").put("firstName", "doe")
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field not found but required: password"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check create user validation, wrong password type")
    @Test
    fun testCreateUserValidationWrongPasswordType(vertx: Vertx, testContext: VertxTestContext) {
        val user = JsonObject().put("email", "email@email.com").put("password", 2).put("lastName", "john").put("firstName", "doe")
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [password] should be a String"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("😀 Check mandatory application/json")
    @Test
    fun testContentType(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                it.request().headers().add("Content-Type", "application/json")
                it.next()
            }
            .handler(jsonContentTypeValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check mandatory application/json wrong type")
    @Test
    fun testContentTypeFail(vertx: Vertx, testContext: VertxTestContext) {
        router.route()
            .handler {
                it.request().headers().add("Content-Type", "fail")
                it.next()
            }
            .handler(jsonContentTypeValidation)
            .failureHandler(verifyFailureHandler(testContext, "Wrong Content-Type header. Actual: fail Expected: application/json"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("😀 Check transaction body validation")
    @Test
    fun testTransactionBodyValidation(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "buy")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")

        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("😀 Check transaction body validation")
    @Test
    fun testTransactionBodyValidation2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "sell")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")

        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("😀 Check transaction body validation wrong source")
    @Test
    fun testTransactionBodyValidationWrongSource(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "MANUAL2")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "sell")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")

        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Check transaction body validation wrong source type")
    @Test
    fun testTransactionBodyValidationWrongSource2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", 3.0)
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "BUY")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [source] should be a String"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Check transaction body validation wrong date")
    @Test
    fun testTransactionBodyValidationWrongDateFormat(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", 0.5)
            .put("type", "BUY")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [date] should be a String"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Check transaction body validation wrong date format")
    @Test
    fun testTransactionBodyValidationWrongDateFormat2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03")
            .put("type", "BUY")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [date] has a wrong format. It should be similar to 2011-12-03T10:15:30+01:00[Europe/Paris]"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Check transaction body validation wrong type")
    @Test
    fun testTransactionBodyValidationWrongType(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "BUYe")
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [type] should be '${Transaction.Type.BUY.toString().toLowerCase()}' or '${Transaction.Type.SELL.toString().toLowerCase()}'"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Check transaction body validation wrong type 2")
    @Test
    fun testTransactionBodyValidationWrongType2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", 2)
            .put("price", 10.0)
            .put("quantity", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [type] should be a String"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("😀 Check login validation")
    @Test
    fun testUserLoginValidation(vertx: Vertx, testContext: VertxTestContext) {
        val loginUser = JsonObject().put("email", "email@email.com").put("password", "eqweqe")
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check login validation email missing")
    @Test
    fun testUserLoginValidationEmailMissing(vertx: Vertx, testContext: VertxTestContext) {
        val loginUser = JsonObject().put("password", "eqweqe")
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [email] missing"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("☹ Check login validation password missing")
    @Test
    fun testUserLoginValidationPasswordMissing(vertx: Vertx, testContext: VertxTestContext) {
        val loginUser = JsonObject().put("email", "email@email.com")
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .failureHandler(verifyFailureHandler(testContext, "Object field [password] missing"))

        vertx.createHttpClient().getNow(port, host, "/", responseHandler(testContext))
    }

    @DisplayName("😀 Get user validation")
    @Test
    fun testGetUserValidation(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        router.get("/users/:userId")
            .handler {
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(getUserValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId", responseHandler(testContext))
    }

    @DisplayName("☹ Get user validation")
    @Test
    fun testGetUserValidationWrongUser(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        router.get("/users/:userId")
            .handler {
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(getUserValidation)
            .failureHandler(verifyFailureHandler(testContext, "User [otherUser] can't be accessed with the given token"))

        vertx.createHttpClient().getNow(port, host, "/users/otherUser", responseHandler(testContext))
    }

    @DisplayName("☹ Upload csv validation")
    @Test
    fun testCsvContentTypeValidation(vertx: Vertx, testContext: VertxTestContext) {
        router.get("/users")
            .handler {
                it.request().headers().add("Content-Type", "derp")
                it.next()
            }
            .handler(csvContentTypeValidation)
            .failureHandler(verifyFailureHandler(testContext, "Wrong Content-Type header. Actual: derp Expected: text/csv"))

        vertx.createHttpClient().getNow(port, host, "/users", responseHandler(testContext))
    }

    @DisplayName("😀 Upload csv validation")
    @Test
    fun testUploadCsvValidation(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val source = "binance"
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.next()
            }
            .handler(uploadCsvValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("😀 Upload csv validation 2")
    @Test
    fun testUploadCsvValidation2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = ","
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .handler(verifySuccessHandler(testContext))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Upload csv validation source fail")
    @Test
    fun testUploadCsvValidationSourceFail(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val source = "BINANCE_FAIL"
        val delimiter = ","
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler(verifyFailureHandler(testContext, "Invalid source [BINANCE_FAIL]"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Upload csv validation delimiter fail")
    @Test
    fun testUploadCsvValidationDelimiterFail(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = "fghkjewlhfjwke"
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler(verifyFailureHandler(testContext, "Invalid delimiter [fghkjewlhfjwke]"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    @DisplayName("☹ Upload csv validation delimiter fail 2")
    @Test
    fun testUploadCsvValidationDelimiterFail2(vertx: Vertx, testContext: VertxTestContext) {
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = "&"
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler(verifyFailureHandler(testContext, "Invalid delimiter [&]"))

        vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions", responseHandler(testContext))
    }

    private fun verifySuccessHandler(testContext: VertxTestContext): Handler<RoutingContext> = Handler {
        testContext.verify {
            assertThat(it.statusCode()).isEqualTo(-1)
            assertThat(it.failed()).isFalse()
            testContext.completeNow()
        }
    }

    private fun verifyFailureHandler(testContext: VertxTestContext, message: String): Handler<RoutingContext> = Handler {
        testContext.verify {
            assertThat(it.failure()).isOfAnyClassIn(ValidationException::class.java)
            assertThat(it.failure().message).isEqualTo(message)
            testContext.completeNow()
        }
    }

    private fun responseHandler(testContext: VertxTestContext): Handler<HttpClientResponse> = Handler {
        testContext.verify {
            testContext.failNow(AssertionError("Something went wrong"))
        }
    }
}
