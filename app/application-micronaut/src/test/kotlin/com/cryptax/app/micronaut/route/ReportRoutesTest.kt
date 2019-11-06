package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.route.Utils.initTransaction
import com.cryptax.app.micronaut.route.Utils.setupRestAssured
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class ReportRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(server.port)
    }

    @AfterAll
    internal fun `after all`() {
        (userRepository as InMemoryUserRepository).deleteAll()
        (transactionRepository as InMemoryTransactionRepository).deleteAll()
    }

    @BeforeEach
    internal fun `before each`() {
        (userRepository as InMemoryUserRepository).deleteAll()
        (transactionRepository as InMemoryTransactionRepository).deleteAll()
    }

    @Test
    fun `generate report`() {
        // given
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("debug", false).
        get("/users/$userId/report").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
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
            assertThat().body("breakdown.ETH.lines[0].metadata", nullValue())
        // @formatter:on
    }

    @Test
    fun `generate report with metadata`() {
        // given
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("debug", true).
        get("/users/$userId/report").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
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
            assertThat().body("breakdown.BTC.lines[0].metadata.currency2UsdValue", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.quantityCurrency2", equalTo(0.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.capitalGainShort", equalTo(00.0f)).
            assertThat().body("breakdown.BTC.lines[0].metadata.capitalGainLong", equalTo(00.0f)).
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
            assertThat().body("breakdown.ETH.lines[0].metadata.currency2UsdValue", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.quantityCurrency2", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.capitalGainShort", equalTo(0.0f)).
            assertThat().body("breakdown.ETH.lines[0].metadata.capitalGainLong", equalTo(0.0f))
        // @formatter:on
    }
}
