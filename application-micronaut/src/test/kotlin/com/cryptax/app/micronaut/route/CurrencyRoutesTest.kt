package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.route.Utils.initUserAndGetToken
import com.cryptax.app.micronaut.route.Utils.setupRestAssured
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class CurrencyRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @BeforeAll
    fun `before all`() {
        setupRestAssured(server.port)
    }

    @Test
    fun `get all currencies`() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/currencies").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("code", hasItems("ADA", "ARK", "BTC", "EOS", "ETH", "ETHOS")).
            assertThat().body("name", hasItems("Cardano", "Ark", "Bitcoin", "Ethereum", "Ethos", "Euro")).
            assertThat().body("symbol", hasItems("ADA", "ARK", "฿", "Ξ", "€", "$")).
            assertThat().body("type", hasItems("crypto", "fiat"))
        // @formatter:on
    }

    @Test
    fun `get all currencies without any token`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/currencies").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }
}
