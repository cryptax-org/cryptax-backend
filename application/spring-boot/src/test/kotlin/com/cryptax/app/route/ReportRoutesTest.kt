package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.initTransaction
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DisplayName("Report routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ReportRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @Autowired
    lateinit var userRepository: InMemoryUserRepository

    @Autowired
    lateinit var transactionRepository: InMemoryTransactionRepository

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured(randomServerPort.toInt())
    }

    @AfterAll
    internal fun afterAll() {
        userRepository.deleteAll()
        transactionRepository.deleteAll()
    }

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        transactionRepository.deleteAll()
    }

    @Test
    @DisplayName("Generate report")
    fun `generate report`() {
        // given
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            queryParam("debug", false).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
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
    @DisplayName("Generate report with metadata")
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
