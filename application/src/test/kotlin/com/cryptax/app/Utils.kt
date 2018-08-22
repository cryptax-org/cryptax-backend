package com.cryptax.app

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.config.objectMapper
import com.cryptax.config.AppConfig
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.config.HttpClientConfig
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import io.vertx.core.json.JsonObject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsNull.nullValue
import java.time.format.DateTimeFormatter

val user: UserWeb = objectMapper.readValue(AppConfig::class.java.getResourceAsStream("/User1.json"), UserWeb::class.java)
val transaction: TransactionWeb = objectMapper.readValue(AppConfig::class.java.getResourceAsStream("/Transaction1.json"), TransactionWeb::class.java)
val transaction2: TransactionWeb = objectMapper.readValue(AppConfig::class.java.getResourceAsStream("/Transaction2.json"), TransactionWeb::class.java)
val credentials = JsonObject().put("email", user.email).put("password", user.password!!.joinToString("")).toString()
val transactionsBinance = AppConfig::class.java.getResource("/Binance-Trade-History.csv").readText()
val transactionsCoinbase = AppConfig::class.java.getResource("/Coinbase-Trade-History.csv").readText()
val transactionsCoinbase2 = AppConfig::class.java.getResource("/Coinbase-Trade-History2.csv").readText()
val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

fun createUser(): Pair<User, String> {
    // @formatter:off
    val response =  given().
                        log().ifValidationFails().
                        body(user).
                        contentType(ContentType.JSON).
                    post("/users").
                    then().
                        log().ifValidationFails().
                        assertThat().statusCode(200).
                        assertThat().body("id", notNullValue()).
                        assertThat().body("email", equalTo(user.email)).
                        assertThat().body("password", nullValue()).
                        assertThat().body("lastName", equalTo(user.lastName)).
                        assertThat().body("firstName", equalTo(user.firstName)).
                    extract()
                        .response()
    // @formatter:on
    response.header("welcomeToken")
    return Pair(User(
        id = response.body.jsonPath().getString("id"),
        email = response.body.jsonPath().getString("email"),
        password = CharArray(1),
        lastName = response.body.jsonPath().getString("lastName"),
        firstName = response.body.jsonPath().getString("firstName")
    ), response.header("welcomeToken"))
}

fun validateUser(pair: Pair<User, String>) {
    // @formatter:off
    given().
        log().ifValidationFails().
        queryParam("token", pair.second).
    get("/users/${pair.first.id}/allow").
    then().
        log().ifValidationFails().
        assertThat().statusCode(200)
    // @formatter:on
}

fun getToken(): JsonPath {
    // @formatter:off
    return  given().
                log().ifValidationFails().
                body(credentials).
                contentType(ContentType.JSON).
            post("/token").
                then().
                log().ifValidationFails().
                assertThat().statusCode(200).
                assertThat().body("token", notNullValue()).
                assertThat().body("refreshToken", notNullValue()).
            extract().
                body().jsonPath()
     // @formatter:on
}

fun addTransaction(id: String, token: JsonPath): JsonPath {
    // @formatter:off
    return  given().
                log().ifValidationFails().
                body(transaction).
                contentType(ContentType.JSON).
                header(Header("Authorization", "Bearer ${token.getString("token")}")).
            post("/users/$id/transactions").
            then().
                log().ifValidationFails().
                assertThat().statusCode(200).
                assertThat().body("id", notNullValue()).
                assertThat().body("userId", nullValue()).
                assertThat().body("date", equalTo(transaction.date.format(formatter))).
                assertThat().body("type", equalTo(transaction.type.toString().toLowerCase())).
                assertThat().body("price", equalTo(10.0f)).
                assertThat().body("quantity", equalTo(2.0f)).
                assertThat().body("currency1", equalTo(transaction.currency1.toString())).
                assertThat().body("currency2", equalTo(transaction.currency2.toString())).
            extract().
                body().jsonPath()
    // @formatter:on
}

fun initUser() {
    val pair = createUser()
    validateUser(pair)
}

fun initUserAndGetToken(): JsonPath {
    val pair = createUser()
    validateUser(pair)
    return getToken()
}

fun initTransaction(): Pair<String, JsonPath> {
    val pair = createUser()
    validateUser(pair)
    val token = getToken()
    addTransaction(pair.first.id, token)
    return Pair(pair.first.id, token)
}

fun setupRestAssured() {
    System.setProperty("PROFILE", "it")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
    val appConfig = TestAppConfig()
    RestAssured.port = appConfig.properties.server.port
    RestAssured.baseURI = "http://" + appConfig.properties.server.domain
    RestAssured.config = RestAssuredConfig
        .config()
        .objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper })
        .httpClient(HttpClientConfig.httpClientConfig()
            .setParam("http.connection.timeout", 3000)
            .setParam("http.socket.timeout", 3000))
}
