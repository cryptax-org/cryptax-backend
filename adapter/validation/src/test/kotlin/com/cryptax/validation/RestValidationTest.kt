package com.cryptax.validation

import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.csvContentTypeValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.loginValidation
import com.cryptax.validation.RestValidation.transactionBodyValidation
import com.cryptax.validation.RestValidation.uploadCsvValidation
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.impl.JWTUser
import io.vertx.ext.web.Router
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * ☢☢ Those are not very reliable tests. This should be updated to assert the right failure
 */
@DisplayName("Request validation validation")
@ExtendWith(VertxExtension::class)
class RestValidationTest {

    private val host = "localhost"
    private val port = 8282

    @DisplayName("😀 Check create user validation")
    @Test
    fun testCreateUserValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val user = JsonObject().put("email", "email@email.com").put("password", "123").put("lastName", "john").put("firstName", "doe")
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check create user validation, body null")
    @Test
    fun testCreateUserValidationBodyNull(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler(createUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Body is null or empty")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check create user validation, empty body")
    @Test
    fun testCreateUserValidationBodyEmpty(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.body = Buffer.buffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Body is null or empty")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }

    }

    @DisplayName("☹ Check create user validation, empty json body")
    @Test
    fun testCreateUserValidationBodyEmptyJson(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.body = JsonObject().toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Body is null or empty")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }

    }

    @DisplayName("☹ Check create user validation, missing password")
    @Test
    fun testCreateUserValidationMissingPassword(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        val user = JsonObject().put("email", "email@email.com").put("lastName", "john").put("firstName", "doe")
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field not found but required: password")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check create user validation, wrong password type")
    @Test
    fun testCreateUserValidationWrongPasswordType(vertx: Vertx, testContext: VertxTestContext) {
        val user = JsonObject().put("email", "email@email.com").put("password", 2).put("lastName", "john").put("firstName", "doe")
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.body = user.toBuffer()
                it.next()
            }
            .handler(createUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [password] should be a String")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Check mandatory application/json")
    @Test
    fun testContentType(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.request().headers().add("Content-Type", "application/json")
                it.next()
            }
            .handler(jsonContentTypeValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check mandatory application/json wrong type")
    @Test
    fun testContentTypeFail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.route()
            .handler {
                it.request().headers().add("Content-Type", "fail")
                it.next()
            }
            .handler(jsonContentTypeValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Wrong Content-Type header. Actual: fail Expected: application/json")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Check transaction body validation")
    @Test
    fun testTransactionBodyValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "buy")
            .put("price", 10.0)
            .put("amount", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check transaction body validation wrong source")
    @Test
    fun testTransactionBodyValidationWrongSource(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "MANUAL2")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "BUY")
            .put("price", 10.0)
            .put("amount", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [source] should be '${Source.MANUAL.toString().toLowerCase()}', was [MANUAL2]")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check transaction body validation wrong date format")
    @Test
    fun testTransactionBodyValidationWrongDateFormat(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03")
            .put("type", "BUY")
            .put("price", 10.0)
            .put("amount", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [date] has a wrong format. It should be similar to 2011-12-03T10:15:30+01:00[Europe/Paris]")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check transaction body validation wrong type")
    @Test
    fun testTransactionBodyValidationWrongType(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", "BUYe")
            .put("price", 10.0)
            .put("amount", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [type] should be '${Transaction.Type.BUY.toString().toLowerCase()}' or '${Transaction.Type.SELL.toString().toLowerCase()}'")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check transaction body validation wrong type 2")
    @Test
    fun testTransactionBodyValidationWrongType2(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val transaction = JsonObject()
            .put("source", "manual")
            .put("date", "2011-12-03T10:15:30Z")
            .put("type", 2)
            .put("price", 10.0)
            .put("amount", 5.0)
            .put("currency1", "BTC")
            .put("currency2", "ETH")
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                it.body = transaction.toBuffer()
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(transactionBodyValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [type] should be a String")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Check login validation")
    @Test
    fun testUserLoginValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val loginUser = JsonObject().put("email", "email@email.com").put("password", "eqweqe")
        val router = Router.router(vertx)
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check login validation email missing")
    @Test
    fun testUserLoginValidationEmailMissing(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val loginUser = JsonObject()
            .put("password", "eqweqe")
        val router = Router.router(vertx)
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [email] missing")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Check login validation password missing")
    @Test
    fun testUserLoginValidationPasswordMissing(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val loginUser = JsonObject().put("email", "email@email.com")
        val router = Router.router(vertx)
        router.get("/")
            .handler {
                it.body = loginUser.toBuffer()
                it.next()
            }
            .handler(loginValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Object field [password] missing")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Get user validation")
    @Test
    fun testGetUserValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val router = Router.router(vertx)
        router.get("/users/:userId")
            .handler {
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(getUserValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/$userId") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Get user validation")
    @Test
    fun testGetUserValidationWrongUser(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val router = Router.router(vertx)
        router.get("/users/:userId")
            .handler {
                it.setUser(JWTUser(JsonObject().put("id", userId), ""))
                it.next()
            }
            .handler(getUserValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "User [otherUser] can't be accessed with the given token")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users/otherUser") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Upload csv validation")
    @Test
    fun testCsvContentTypeValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val router = Router.router(vertx)
        router.get("/users")
            .handler {
                it.request().headers().add("Content-Type", "derp")
                it.next()
            }
            .handler(csvContentTypeValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Wrong Content-Type header. Actual: derp Expected: text/csv")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    vertx.createHttpClient().getNow(port, host, "/users") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Upload csv validation")
    @Test
    fun testUploadCsvValidation(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val source = "binance"
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.next()
            }
            .handler(uploadCsvValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("😀 Upload csv validation 2")
    @Test
    fun testUploadCsvValidation2(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = ","
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .handler {
                testContext.verify {
                    assertEquals(-1, it.statusCode())
                    assertFalse(it.failed())
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Upload csv validation source fail")
    @Test
    fun testUploadCsvValidationSourceFail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val source = "BINANCE_FAIL"
        val delimiter = ","
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Invalid source [BINANCE_FAIL]")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Upload csv validation delimiter fail")
    @Test
    fun testUploadCsvValidationDelimiterFail(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = "fghkjewlhfjwke"
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Invalid delimiter [fghkjewlhfjwke]")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }

    @DisplayName("☹ Upload csv validation delimiter fail 2")
    @Test
    fun testUploadCsvValidationDelimiterFail2(vertx: Vertx, testContext: VertxTestContext) {
        // given
        val userId = "randomId"
        val source = "BINANCE"
        val delimiter = "&"
        val router = Router.router(vertx)
        router.get("/users/:userId/transactions")
            .handler {
                // Not sure we need to add two times the same param
                it.request().params().add("source", source)
                it.queryParams().add("source", source)
                it.request().params().add("delimiter", delimiter)
                it.queryParams().add("delimiter", delimiter)
                it.next()
            }
            .handler(uploadCsvValidation)
            .failureHandler {
                testContext.verify {
                    assert(it.failure() is ValidationException)
                    assert((it.failure() as ValidationException).message == "Invalid delimiter [&]")
                    testContext.completeNow()
                }
            }

        vertx.createHttpServer()
            .requestHandler { router.accept(it) }
            .listen(port) { res ->
                if (res.succeeded()) {
                    // when
                    val client = vertx.createHttpClient()
                    client.getNow(port, host, "/users/$userId/transactions") { fail() }
                } else {
                    fail("The server did not start")
                }
            }
    }
}
