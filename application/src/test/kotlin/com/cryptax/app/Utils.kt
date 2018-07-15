package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.User
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import io.vertx.core.json.JsonObject
import org.hamcrest.Matchers
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull

val user = Config.objectMapper.readValue(Config::class.java.getResourceAsStream("/User1.json"), User::class.java)
val transaction = Config.objectMapper.readValue(Config::class.java.getResourceAsStream("/Transaction1.json"), TransactionWeb::class.java)
val credentials = JsonObject().put("email", user.email).put("password", user.password.joinToString("")).toString()

fun createUser(): String {
    // @formatter:off
    return  given().
                log().all().
                body(user).
                contentType(ContentType.JSON).
            post("/users").
            then().
                log().all().
                assertThat().body("id", notNullValue()).
                assertThat().body("email", IsEqual(user.email)).
                assertThat().body("password", IsNull.nullValue()).
                assertThat().body("lastName", IsEqual(user.lastName)).
                assertThat().body("firstName", IsEqual(user.firstName)).
                assertThat().statusCode(200).
            extract()
                .body().jsonPath().getString("id")
    // @formatter:on
}

fun getToken(): JsonPath {
    // @formatter:off
    return  given().
                log().all().
                body(credentials).
                contentType(ContentType.JSON).
            post("/token").
                then().
                log().all().
                assertThat().body("token", notNullValue()).
                assertThat().body("refreshToken", notNullValue()).
                assertThat().statusCode(200).
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
                assertThat().body("id", notNullValue()).
                assertThat().body("userId", IsNull.nullValue()).
                // FIXME check how to validate dates
                //assertThat().body("date", IsEqual(transaction.date)).
                assertThat().body("type", Matchers.equalTo(transaction.type.toString())).
                assertThat().body("price", Matchers.equalTo(10.0f)).
                assertThat().body("amount", Matchers.equalTo(2.0f)).
                assertThat().body("currency1", Matchers.equalTo(transaction.currency1.toString())).
                assertThat().body("currency2", Matchers.equalTo(transaction.currency2.toString())).
                assertThat().statusCode(200).
            extract().
                body().jsonPath()
    // @formatter:on
}
