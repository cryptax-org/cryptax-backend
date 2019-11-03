package com.cryptax.app.micronaut

import com.cryptax.app.micronaut.config.JacksonConfig
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.config.HttpClientConfig
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.TimeZone

object Utils {
    val objectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())
        .registerModule(JacksonConfig.EnumModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    val user: UserWeb = objectMapper.readValue(Utils::class.java.getResourceAsStream("/user.json"), UserWeb::class.java)
    val credentials = JsonNodeFactory.instance.objectNode().put("email", user.email).put("password", user.password!!.joinToString("")).toString()
    val transaction: TransactionWeb = objectMapper.readValue(Utils::class.java.getResourceAsStream("/transaction.json"), TransactionWeb::class.java)
    val transaction2: TransactionWeb = objectMapper.readValue(Utils::class.java.getResourceAsStream("/transaction2.json"), TransactionWeb::class.java)
    val transaction3: TransactionWeb = objectMapper.readValue(Utils::class.java.getResourceAsStream("/transaction3.json"), TransactionWeb::class.java)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun setupRestAssured(port: Int) {
        RestAssured.port = port
        RestAssured.config = RestAssuredConfig
            .config()
            .objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper })
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", 3000)
                .setParam("http.socket.timeout", 3000))
    }

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

    fun initiatePasswordReset(pair: Pair<User, String>): JsonPath {
        // @formatter:off
        return given().
                    log().ifValidationFails().
                get("/users/email/${pair.first.email}/reset").
                then().
                    log().ifValidationFails().
                    assertThat().statusCode(200).
                    assertThat().body("token", notNullValue()).
                extract().
                    body().jsonPath()
        // @formatter:on
    }

    fun initUserAndGetToken(): JsonPath {
        val pair = createUser()
        validateUser(pair)
        return getToken()
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
                    assertThat().body("date", equalTo(transaction.date!!.format(formatter))).
                    assertThat().body("type", equalTo(transaction.type.toString().toLowerCase())).
                    assertThat().body("price", equalTo(10.0f)).
                    assertThat().body("quantity", equalTo(2.0f)).
                    assertThat().body("currency1", equalTo(transaction.currency1.toString())).
                    assertThat().body("currency2", equalTo(transaction.currency2.toString())).
                extract().
                    body().jsonPath()
        // @formatter:on
    }

    fun initTransaction(): Pair<String, JsonPath> {
        val pair = createUser()
        validateUser(pair)
        val token = getToken()
        addTransaction(pair.first.id, token)
        return Pair(pair.first.id, token)
    }

    fun validateToken(token: String, userId: String, isRefresh: Boolean) {
        val tab = token.split(".")
        val header = objectMapper.readValue(String(Base64.getUrlDecoder().decode(tab[0].toByteArray())), JsonNode::class.java)
        val body = objectMapper.readValue(String(Base64.getUrlDecoder().decode(tab[1].toByteArray())), JsonNode::class.java)

        assertThat(header.get("alg").textValue()).isEqualTo("HS512")
        assertThat(body.get("jti").textValue()).isNotNull()
        assertThat(body.get("sub").textValue()).isEqualTo(userId)
        assertThat(body.get("iss").textValue()).isEqualTo("Cryptax")
        assertThat(body.get("isRefresh").booleanValue()).isEqualTo(isRefresh)
        assertThat(body.get("auth")[0].textValue()).isEqualTo("USER")
        assertThat(body.get("iat").longValue()).isNotNull()
        assertThat(body.get("exp").longValue()).isNotNull()
    }
}

