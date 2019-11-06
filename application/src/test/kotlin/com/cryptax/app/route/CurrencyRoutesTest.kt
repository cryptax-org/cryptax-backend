package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.initUserAndGetToken
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.config.ProfileType
import com.cryptax.db.InMemoryUserRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DisplayName("Currency routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(ProfileType.test)
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CurrencyRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @Autowired
    lateinit var memory: InMemoryUserRepository

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(randomServerPort.toInt())
    }

    @AfterAll
    internal fun `after all`() {
        memory.deleteAll()
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
            assertThat().body("code", hasItems("ADA", "ARK", "BTC", "EOS", "ETH", "ETHOS")).
            assertThat().body("name", hasItems("Cardano", "Ark", "Bitcoin", "Ethereum", "Ethos", "Euro")).
            assertThat().body("symbol", hasItems("ADA", "ARK", "฿", "Ξ", "€", "$")).
            assertThat().body("type", hasItems("crypto", "fiat"))
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
