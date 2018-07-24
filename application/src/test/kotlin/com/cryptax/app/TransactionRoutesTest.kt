package com.cryptax.app

import com.cryptax.config.AppConfig
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsNull.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Transaction routes integration tests")
class TransactionRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        val appConfig = TestAppConfig()
        RestAssured.port = appConfig.properties.server.port
        RestAssured.baseURI = "http://" + appConfig.properties.server.domain
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> AppConfig.objectMapper })
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestAppConfig()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Add a transaction")
    fun addTransaction(testContext: VertxTestContext) {
        initTransaction()
        testContext.completeNow()
    }

    @Test
    @DisplayName("Get all transactions for a user")
    fun getAllTransactions(testContext: VertxTestContext) {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            body(transaction).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/$userId/transactions").
        then().
            log().all().
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].userId", nullValue()).
            // FIXME check how to validate dates
            //assertThat().body("[0].date", IsEqual(transaction.date)).
            assertThat().body("[0].type", equalTo(transaction.type.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(10.0f)).
            assertThat().body("[0].amount", equalTo(2.0f)).
            assertThat().body("[0].currency1", equalTo(transaction.currency1.toString())).
            assertThat().body("[0].currency2", equalTo(transaction.currency2.toString())).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one transaction for a user")
    fun getOneTransaction(testContext: VertxTestContext) {
        val result = initTransaction()
        val userId = result.first
        val token = result.second
        val transactionId = addTransaction(userId, token).getString("id")

        // @formatter:off
        given().
            body(transaction).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/$userId/transactions/$transactionId").
        then().
            log().all().
            assertThat().body("id", notNullValue()).
            assertThat().body("userId", nullValue()).
            // FIXME check how to validate dates
            //assertThat().body("[0].date", IsEqual(transaction.date)).
            assertThat().body("type", equalTo(transaction.type.toString().toLowerCase())).
            assertThat().body("price", equalTo(10.0f)).
            assertThat().body("amount", equalTo(2.0f)).
            assertThat().body("currency1", equalTo(transaction.currency1.toString())).
            assertThat().body("currency2", equalTo(transaction.currency2.toString())).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Update one transaction for a user")
    fun updateOneTransaction(testContext: VertxTestContext) {
        val result = initTransaction()
        val userId = result.first
        val token = result.second
        val transactionId = addTransaction(userId, token).getString("id")
        val transactionUpdated = TransactionWeb(
            source = Source.MANUAL,
            date = ZonedDateTime.now(),
            type = Transaction.Type.SELL,
            price = 20.0,
            amount = 5.0,
            currency1 = Currency.BTC,
            currency2 = Currency.ETH)

        // @formatter:off
        given().
            body(transactionUpdated).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        put("/users/$userId/transactions/$transactionId").
        then().
            log().all().
            assertThat().body("id", equalTo(transactionId)).
            assertThat().body("userId", nullValue()).
            // FIXME check how to validate dates
            //assertThat().body("[0].date", IsEqual(transaction.date)).
            assertThat().body("type", equalTo(transactionUpdated.type.toString().toLowerCase())).
            assertThat().body("price", equalTo(20.0f)).
            assertThat().body("amount", equalTo(5.0f)).
            assertThat().body("currency1", equalTo(transactionUpdated.currency1.toString())).
            assertThat().body("currency2", equalTo(transactionUpdated.currency2.toString())).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Upload a Binance CSV")
    fun uploadBinanceCsv(testContext: VertxTestContext) {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            body(transactionsBinance).
            contentType("text/csv").
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","binance").
        post("/users/$userId/transactions/upload").
        then().
            log().all().
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo(Source.BINANCE.toString().toLowerCase())).
            // FIXME check how to validate dates
            assertThat().body("[0].date", notNullValue()).
            assertThat().body("[0].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(0.009776f)).
            assertThat().body("[0].amount", equalTo(150.13f)).
            assertThat().body("[0].currency1", equalTo(Currency.ICON.code)).
            assertThat().body("[0].currency2", equalTo(Currency.ETH.code)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Upload a Coinbase CSV")
    fun uploadCoinbaseCsv(testContext: VertxTestContext) {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            body(transactionsCoinbase).
            contentType("text/csv").
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","coinbase").
        post("/users/$userId/transactions/upload").
        then().
            log().all().
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo(Source.COINBASE.toString().toLowerCase())).
            // FIXME check how to validate dates
            assertThat().body("[0].date", notNullValue()).
            assertThat().body("[0].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(6417.48f)).
            assertThat().body("[0].amount", equalTo(0.18730723f)).
            assertThat().body("[0].currency1", equalTo(Currency.BTC.code)).
            assertThat().body("[0].currency2", equalTo(Currency.USD.code)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
