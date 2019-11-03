package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.Utils.setupRestAssured
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class InfoRoutesTest {

    @Inject
    lateinit var server: EmbeddedServer

    @BeforeAll
    fun beforeAll() {
        setupRestAssured(server.port)
    }

    /*@Test
    fun `get info about the app`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/info").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("Manifest-Version", notNullValue()).
            assertThat().body("Implementation-Title", notNullValue()).
            assertThat().body("Implementation-Version", notNullValue()).
            assertThat().body("Built-Status", notNullValue()).
            assertThat().body("Built-By", notNullValue()).
            assertThat().body("Built-OS", notNullValue()).
            assertThat().body("Build-Date", notNullValue()).
            assertThat().body("Gradle-Version", notNullValue()).
            assertThat().body("Module-Source", notNullValue()).
            assertThat().body("Module-Origin", notNullValue()).
            assertThat().body("Change", notNullValue()).
            assertThat().body("Branch", notNullValue()).
            assertThat().body("Build-Host", notNullValue()).
            assertThat().body("Build-Job", notNullValue()).
            assertThat().body("Build-Number", notNullValue()).
            assertThat().body("Build-Id", notNullValue()).
            assertThat().body("Created-By", notNullValue()).
            assertThat().body("Build-Java-Version", notNullValue()).
            assertThat().body("X-Compile-Target-JDK", notNullValue()).
            assertThat().body("X-Compile-Source-JDK", notNullValue())
        // @formatter:on
    }*/

    @Test
    fun `get health data`() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/health").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("transactionRepository.healthy", equalTo(true)).
            assertThat().body("transactionRepository", hasKey("timestamp")).
            assertThat().body("userRepository.healthy", equalTo(true)).
            assertThat().body("userRepository", hasKey("timestamp"))
        // @formatter:on
    }

    @Test
    fun ping() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/ping").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("result", equalTo("pong"))
        // @formatter:on
    }

    @Test
    fun root() {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }
}
