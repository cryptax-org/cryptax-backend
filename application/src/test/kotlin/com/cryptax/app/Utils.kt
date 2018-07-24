package com.cryptax.app

import com.cryptax.config.AppConfig
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.model.UserWeb
import com.cryptax.db.InMemoryTransactionRepository
import com.cryptax.db.InMemoryUserRepository
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.id.JugIdGenerator
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import io.vertx.core.json.JsonObject
import org.hamcrest.Matchers
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val user = AppConfig.objectMapper.readValue(AppConfig::class.java.getResourceAsStream("/User1.json"), UserWeb::class.java)
val transaction = AppConfig.objectMapper.readValue(AppConfig::class.java.getResourceAsStream("/Transaction1.json"), TransactionWeb::class.java)
val credentials = JsonObject().put("email", user.email).put("password", user.password!!.joinToString("")).toString()
val transactionsBinance = AppConfig::class.java.getResource("/Binance-Trade-History.csv").readText()
val transactionsCoinbase = AppConfig::class.java.getResource("/Coinbase-Trade-History.csv").readText()

fun createUser(): Pair<User, String> {
    // @formatter:off
    val response =  given().
                            log().all().
                            body(user).
                            contentType(ContentType.JSON).
                        post("/users").
                        then().
                            log().all().
                            assertThat().statusCode(200).
                            assertThat().body("id", notNullValue()).
                            assertThat().body("email", IsEqual(user.email)).
                            assertThat().body("password", IsNull.nullValue()).
                            assertThat().body("lastName", IsEqual(user.lastName)).
                            assertThat().body("firstName", IsEqual(user.firstName)).
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
        log().all().
        queryParam("token", pair.second).
    get("/users/${pair.first.id}/allow").
    then().
        log().all().
        assertThat().statusCode(200)
    // @formatter:on
}

private fun getToken(): JsonPath {
    // @formatter:off
    return  given().
                log().all().
                body(credentials).
                contentType(ContentType.JSON).
            post("/token").
                then().
                log().all().
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
                log().all().
                body(transaction).
                contentType(ContentType.JSON).
                header(Header("Authorization", "Bearer ${token.getString("token")}")).
            post("/users/$id/transactions").
            then().
                log().all().
                assertThat().statusCode(200).
                assertThat().body("id", notNullValue()).
                assertThat().body("userId", IsNull.nullValue()).
                // FIXME check how to validate dates
                //assertThat().body("date", IsEqual(transaction.date)).
                assertThat().body("type", Matchers.equalTo(transaction.type.toString().toLowerCase())).
                assertThat().body("price", Matchers.equalTo(10.0f)).
                assertThat().body("amount", Matchers.equalTo(2.0f)).
                assertThat().body("currency1", Matchers.equalTo(transaction.currency1.toString())).
                assertThat().body("currency2", Matchers.equalTo(transaction.currency2.toString())).
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
    addTransaction(pair.first.id!!, token)
    return Pair(pair.first.id!!, token)
}

private fun testKodein(): Kodein {
    return Kodein {
        bind<UserRepository>() with singleton { InMemoryUserRepository() }
        bind<TransactionRepository>() with singleton { InMemoryTransactionRepository() }
        bind<IdGenerator>() with singleton { JugIdGenerator() }
        bind<EmailService>() with singleton { EmailServiceStub() }
    }
}

class TestAppConfig : AppConfig("it", testKodein())

class EmailServiceStub : EmailService {

    override fun welcomeEmail(user: User, token: String) {
    }
}
