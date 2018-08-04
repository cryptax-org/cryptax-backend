package com.cryptax.app.metrics

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.setupRestAssured
import com.cryptax.app.verticle.RestVerticle
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Metrics routes integration tests")
class MetricsTest {

    lateinit var vertx: Vertx

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured()
    }

    @BeforeEach
    fun beforeEach(testContext: VertxTestContext) {
        vertx = Vertx.vertx(VertxOptions().setMetricsOptions(DropwizardMetricsOptions(baseName = "cryptax", enabled = true)))
        vertx.deployVerticle(RestVerticle(TestAppConfig()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @AfterEach
    fun afterEach(testContext: VertxTestContext) {
        vertx.close { ar ->
            if (ar.succeeded()) testContext.completeNow()
            else testContext.failNow(AssertionError())
        }
    }

    @Test
    @DisplayName("Ping the server")
    fun testPing(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        `when`().
            get("/ping").
        then().
            log().ifValidationFails().
            assertThat().body("result", equalTo("pong")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get metrics from the server")
    fun testOneMetric(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        `when`().
            get("/metrics").
        then().
            log().ifValidationFails().
            assertThat().body("$", notNullValue()).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get one metric from the server")
    fun testMetrics(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            queryParam("key","cryptax.http.servers.0.0.0.0:8080.connections").
        `when`().
            get("/metrics").
        then().
            log().ifValidationFails().
            assertThat().body("type", equalTo("timer")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get metrics available from the server")
    fun testMetricsAvailable(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        `when`().
            get("/metrics/available").
        then().
            log().ifValidationFails().
            assertThat().body("[0]", equalTo("cryptax.http.servers.0.0.0.0:8080.connections")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
