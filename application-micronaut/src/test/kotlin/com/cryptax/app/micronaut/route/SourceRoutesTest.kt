package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.Utils.initUserAndGetToken
import com.cryptax.app.micronaut.Utils.setupRestAssured
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@DisplayName("Source routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class SourceRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @BeforeAll
    internal fun `before all`() {
        setupRestAssured(server.port)
    }

    @DisplayName("Get all sources")
    @Test
    fun `get all sources`() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/sources").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("$", hasItems("coinbase", "binance", "kucoin", "unknown"))
        // @formatter:on
    }

    @DisplayName("Get all sources without any token")
    @Test
    fun `get all sources without any token`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/sources").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }
}
