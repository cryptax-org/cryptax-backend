package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.initUserAndGetToken
import com.cryptax.app.route.Utils.setupRestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DisplayName("Token routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CurrencyRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured(randomServerPort.toInt())
    }

    @DisplayName("Get all currencies")
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
            body("code", hasItems("ADA", "ARK", "BTC", "EOS", "ETH", "ETHOS")).
            body("name", hasItems("Cardano", "Ark", "Bitcoin", "Ethereum", "Ethos", "Euro")).
            body("symbol", hasItems("ADA", "ARK", "฿", "Ξ", "€", "$")).
            body("type", hasItems("crypto", "fiat"))
        // @formatter:on
    }

    @DisplayName("Get all currencies without any token")
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
