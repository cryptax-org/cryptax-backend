package com.cryptax.app.routes

import com.cryptax.app.config.TestConfig
import com.cryptax.app.config.kodein
import com.cryptax.app.setupRestAssured
import com.cryptax.app.verticle.RestVerticle
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Info routes integration tests")
class InfoRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured()
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestConfig(), kodein()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Get info")
    fun testInfo(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/info").
        then().
            log().ifValidationFails().
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
            assertThat().body("X-Compile-Source-JDK", notNullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
