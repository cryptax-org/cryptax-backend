package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.addTransaction
import com.cryptax.app.route.Utils.createUser
import com.cryptax.app.route.Utils.formatter
import com.cryptax.app.route.Utils.getToken
import com.cryptax.app.route.Utils.initTransaction
import com.cryptax.app.route.Utils.objectMapper
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.app.route.Utils.transaction
import com.cryptax.app.route.Utils.transaction2
import com.cryptax.app.route.Utils.transaction3
import com.cryptax.app.route.Utils.validateUser
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
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
import java.io.File
import java.time.ZonedDateTime

@DisplayName("Transaction routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TransactionRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @Autowired
    lateinit var userRepository: InMemoryUserRepository

    @Autowired
    lateinit var transactionRepository: InMemoryTransactionRepository

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(randomServerPort.toInt())
    }

    @AfterAll
    internal fun `after all`() {
        userRepository.deleteAll()
        transactionRepository.deleteAll()
    }

    @BeforeEach
    fun `before each`() {
        userRepository.deleteAll()
        transactionRepository.deleteAll()
    }

    @DisplayName("Add a transaction")
    @Test
    fun `add a transaction`() {
        initTransaction()
    }

    @Test
    @DisplayName("Add multiple transactions")
    fun `add multiple transactions`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second
        val transactions: List<TransactionWeb> = objectMapper.readValue(
            Utils::class.java.getResourceAsStream("/transactions.json"),
            objectMapper.typeFactory.constructCollectionType(List::class.java, TransactionWeb::class.java)
        )

        // @formatter:off
        given().
            body(transactions).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        post("/users/$userId/transactions/bulk").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo("manual")).
            assertThat().body("[0].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("[0].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(10.0f)).
            assertThat().body("[0].quantity", equalTo(2.0f)).
            assertThat().body("[0].currency1", equalTo(Currency.ETH.code)).
            assertThat().body("[0].currency2", equalTo(Currency.BTC.code)).
            assertThat().body("[1].id", notNullValue()).
            assertThat().body("[1].source", equalTo("random source")).
            assertThat().body("[1].date", equalTo("2011-12-03T10:15:30Z")).
            assertThat().body("[1].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[1].price", equalTo(1000.0f)).
            assertThat().body("[1].quantity", equalTo(5.0f)).
            assertThat().body("[1].currency1", equalTo(Currency.BTC.code)).
            assertThat().body("[1].currency2", equalTo(Currency.ETH.code))
        // @formatter:on
    }

    @DisplayName("Add a transaction with custom source")
    @Test
    fun `add a transaction with a custom source`() {
        val pair = createUser()
        validateUser(pair)
        val token = getToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(transaction2).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        post("/users/${pair.first.id}/transactions").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("id", notNullValue()).
            assertThat().body("userId", nullValue()).
            assertThat().body("date", equalTo(transaction.date!!.format(formatter))).
            assertThat().body("type", equalTo(transaction.type!!.name.toLowerCase())).
            assertThat().body("price", equalTo(10.0f)).
            assertThat().body("quantity", equalTo(2.0f)).
            assertThat().body("currency1", equalTo(transaction.currency1.toString())).
            assertThat().body("currency2", equalTo(transaction.currency2.toString()))
        // @formatter:on
    }

    @DisplayName("Add a transaction, test validation")
    @Test
    fun `add a transaction, test validation`() {
        val pair = createUser()
        validateUser(pair)
        val token = getToken()
        val transaction = TransactionWeb()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(transaction).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        post("/users/${pair.first.id}/transactions").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details", hasItems(
                                                        "Source can not be empty",
                                                        "Date can not be null",
                                                        "Type can not be null",
                                                        "Price can not be null",
                                                        "Quantity can not be null",
                                                        "Currency1 can not be null",
                                                        "Currency2 can not be null"))
        // @formatter:on
    }

    @DisplayName("Add a transaction with negative price")
    @Test
    fun `add transaction with negative price`() {
        val pair = createUser()
        validateUser(pair)
        val token = getToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(transaction3).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        post("/users/${pair.first.id}/transactions").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Price can't be negative"))
        // @formatter:on
    }

    @DisplayName("Get all transactions for a user")
    @Test
    fun `get all transactions`() {
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
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].userId", nullValue()).
            assertThat().body("[0].date", equalTo(transaction.date!!.format(formatter))).
            assertThat().body("[0].type", equalTo(transaction.type.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(10.0f)).
            assertThat().body("[0].quantity", equalTo(2.0f)).
            assertThat().body("[0].currency1", equalTo(transaction.currency1.toString())).
            assertThat().body("[0].currency2", equalTo(transaction.currency2.toString()))
        // @formatter:on
    }

    @DisplayName("Get one transaction for a user")
    @Test
    fun `get one transaction`() {
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
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("id", notNullValue()).
            assertThat().body("userId", nullValue()).
            assertThat().body("date", equalTo(transaction.date!!.format(formatter))).
            assertThat().body("type", equalTo(transaction.type.toString().toLowerCase())).
            assertThat().body("price", equalTo(10.0f)).
            assertThat().body("quantity", equalTo(2.0f)).
            assertThat().body("currency1", equalTo(transaction.currency1.toString())).
            assertThat().body("currency2", equalTo(transaction.currency2.toString()))
        // @formatter:on
    }

    @DisplayName("Get one transaction not found for a user")
    @Test
    fun `get transaction, not found`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            body(transaction).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/$userId/transactions/ehjlwqhjeklqjewqkle").
        then().
            log().ifValidationFails().
            assertThat().statusCode(404)
        // @formatter:on
    }

    @DisplayName("Update one transaction for a user")
    @Test
    fun `update one transaction`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        val transactionId = addTransaction(userId, token).getString("id")
        val transactionUpdated = TransactionWeb(
            id = null,
            source = Source.UNKNOWN.name.toLowerCase(),
            date = ZonedDateTime.now(),
            type = Transaction.Type.SELL,
            price = 20.0,
            quantity = 5.0,
            currency1 = Currency.BTC,
            currency2 = Currency.ETH)

        // @formatter:off
        given().
            body(transactionUpdated).
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        put("/users/$userId/transactions/$transactionId").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("id", equalTo(transactionId)).
            assertThat().body("userId", nullValue()).
            assertThat().body("date", notNullValue()).
            assertThat().body("type", equalTo(transactionUpdated.type.toString().toLowerCase())).
            assertThat().body("price", equalTo(20.0f)).
            assertThat().body("quantity", equalTo(5.0f)).
            assertThat().body("currency1", equalTo(transactionUpdated.currency1.toString())).
            assertThat().body("currency2", equalTo(transactionUpdated.currency2.toString()))
        // @formatter:on
    }

    @DisplayName("Delete one transaction for a user")
    @Test
    fun `delete one transaction`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        val transactionId = addTransaction(userId, token).getString("id")

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        delete("/users/$userId/transactions/$transactionId").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @DisplayName("Delete one transaction not found for a user")
    @Test
    fun `delete one transaction, not found`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        delete("/users/$userId/transactions/eweeqewqeq").
        then().
            log().ifValidationFails().
            assertThat().statusCode(404)
        // @formatter:on
    }

    @DisplayName("Upload a Binance CSV")
    @Test
    fun `upload binance csv`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            multiPart(File(Utils::class.java.getResource("/binance-trade-history.csv").toURI())).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","binance").
            log().ifValidationFails().
        post("/users/$userId/transactions/upload").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo(Source.BINANCE.toString().toLowerCase())).
            assertThat().body("[0].date", equalTo("2018-01-09T18:04:24Z")).
            assertThat().body("[0].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(0.009776f)).
            assertThat().body("[0].quantity", equalTo(150.13f)).
            assertThat().body("[0].currency1", equalTo(Currency.ICON.code)).
            assertThat().body("[0].currency2", equalTo(Currency.ETH.code))
        // @formatter:on
    }

    @DisplayName("Upload a Coinbase CSV")
    @Test
    fun `upload coinbase csv`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            multiPart(File(Utils::class.java.getResource("/coinbase-trade-history.csv").toURI())).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","coinbase").
        post("/users/$userId/transactions/upload").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo(Source.COINBASE.toString().toLowerCase())).
            assertThat().body("[0].date", equalTo("2017-10-31T00:00:00Z")).
            assertThat().body("[0].type", equalTo(Transaction.Type.BUY.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(6417.48f)).
            assertThat().body("[0].quantity", equalTo(0.18730723f)).
            assertThat().body("[0].currency1", equalTo(Currency.BTC.code)).
            assertThat().body("[0].currency2", equalTo(Currency.USD.code))
        // @formatter:on
    }

    @DisplayName("Upload a Coinbase CSV 2")
    @Test
    fun `upload coinbase csv 2`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            multiPart(File(Utils::class.java.getResource("/coinbase-trade-history2.csv").toURI())).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","coinbase").
        post("/users/$userId/transactions/upload").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("[0].id", notNullValue()).
            assertThat().body("[0].source", equalTo(Source.COINBASE.toString().toLowerCase())).
            assertThat().body("[0].date", equalTo("2017-10-31T00:00:00Z")).
            assertThat().body("[0].type", equalTo(Transaction.Type.SELL.toString().toLowerCase())).
            assertThat().body("[0].price", equalTo(6417.48f)).
            assertThat().body("[0].quantity", equalTo(0.18730723f)).
            assertThat().body("[0].currency1", equalTo(Currency.BTC.code)).
            assertThat().body("[0].currency2", equalTo(Currency.USD.code))
        // @formatter:on
    }

    @DisplayName("Upload a csv without any csv")
    @Test
    fun `upload csv without any csv`() {
        val result = initTransaction()
        val userId = result.first
        val token = result.second

        // @formatter:off
        given().
            header(Header("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
            queryParam("source","coinbase").
        post("/users/$userId/transactions/upload").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details[0]", equalTo("Csv file is mandatory"))
        // @formatter:on
    }
}
