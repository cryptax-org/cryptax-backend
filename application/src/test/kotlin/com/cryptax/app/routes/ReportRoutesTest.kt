package com.cryptax.app.routes

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.config.objectMapper
import com.cryptax.app.initTransaction
import com.cryptax.app.verticle.RestVerticle
import com.nhaarman.mockitokotlin2.isNull
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
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Report routes integration tests")
class ReportRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        val appConfig = TestAppConfig()
        RestAssured.port = appConfig.properties.server.port
        RestAssured.baseURI = "http://" + appConfig.properties.server.domain
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper })
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestAppConfig()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Generate report")
    fun testGenerateReport(testContext: VertxTestContext) {
        // given
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            queryParam("debug", false).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/$userId/report").
        then().
            log().all().
            assertThat().body("totalCapitalGainShort", equalTo(0.0f)).
            assertThat().body("totalCapitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.capitalGainShort", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.capitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.lines[0].transactionId", notNullValue()).
            assertThat().body("breakdown.BTC.lines[0].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("breakdown.BTC.lines[0].currency1", equalTo("ETH")).
            assertThat().body("breakdown.BTC.lines[0].currency2", equalTo("BTC")).
            assertThat().body("breakdown.BTC.lines[0].type", equalTo("buy")).
            assertThat().body("breakdown.BTC.lines[0].price", equalTo(10.0f)).
            assertThat().body("breakdown.BTC.lines[0].quantity", equalTo(2.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata", nullValue()).
            assertThat().body("breakdown.ETH.capitalGainShort", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.capitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].transactionId", notNullValue()).
            assertThat().body("breakdown.ETH.lines[0].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("breakdown.ETH.lines[0].currency1", equalTo("ETH")).
            assertThat().body("breakdown.ETH.lines[0].currency2", equalTo("BTC")).
            assertThat().body("breakdown.ETH.lines[0].type", equalTo("buy")).
            assertThat().body("breakdown.ETH.lines[0].price", equalTo(10.0f)).
            assertThat().body("breakdown.ETH.lines[0].quantity", equalTo(2.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata", nullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Generate report with metadata")
    fun testGenerateReportWithDebug(testContext: VertxTestContext) {
        // given
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
         given().
            log().all().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("debug", true).
        get("/users/$userId/report").
        then().
            log().all().
            assertThat().body("totalCapitalGainShort", equalTo(0.0f)).
            assertThat().body("totalCapitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.capitalGainShort", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.capitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.lines[0].transactionId", notNullValue()).
            assertThat().body("breakdown.BTC.lines[0].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("breakdown.BTC.lines[0].currency1", equalTo("ETH")).
            assertThat().body("breakdown.BTC.lines[0].currency2", equalTo("BTC")).
            assertThat().body("breakdown.BTC.lines[0].type", equalTo("buy")).
            assertThat().body("breakdown.BTC.lines[0].price", equalTo(10.0f)).
            assertThat().body("breakdown.BTC.lines[0].quantity", equalTo(2.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.ignored", equalTo(true)).
            assertThat().body("breakdown.BTC.lines[0].metadata.currency1UsdValue", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.currency2UsdValue", equalTo(100.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.quantityCurrency2", equalTo(20.0f)).
            assertThat().body("breakdown.ETH.capitalGainShort", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.capitalGainLong", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].transactionId", notNullValue()).
            assertThat().body("breakdown.ETH.lines[0].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("breakdown.ETH.lines[0].currency1", equalTo("ETH")).
            assertThat().body("breakdown.ETH.lines[0].currency2", equalTo("BTC")).
            assertThat().body("breakdown.ETH.lines[0].type", equalTo("buy")).
            assertThat().body("breakdown.ETH.lines[0].price", equalTo(10.0f)).
            assertThat().body("breakdown.ETH.lines[0].quantity", equalTo(2.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.ignored", equalTo(true)).
            assertThat().body("breakdown.ETH.lines[0].metadata.currency1UsdValue", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.currency2UsdValue", equalTo(100.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.quantityCurrency2", equalTo(20.0f)).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
