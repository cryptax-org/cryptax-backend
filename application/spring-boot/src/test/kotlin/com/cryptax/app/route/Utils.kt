package com.cryptax.app.route

import com.cryptax.app.config.JacksonConfig
import com.cryptax.controller.model.UserWeb
import com.cryptax.domain.entity.User
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.config.HttpClientConfig
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue

object Utils {
    private val objectMapper = JacksonConfig().objectMapper()
    val user = objectMapper.readValue(Utils::class.java.getResourceAsStream("/user.json"), UserWeb::class.java)

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
}

