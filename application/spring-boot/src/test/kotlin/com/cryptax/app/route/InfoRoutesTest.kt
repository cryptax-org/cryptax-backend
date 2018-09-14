package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.setupRestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.hasKey
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
class InfoRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured(randomServerPort.toInt())
    }

    @DisplayName("Get info about the app")
    @Test
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
    }

    @DisplayName("Get health data")
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

    @DisplayName("Ping")
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

    @DisplayName("Root")
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
