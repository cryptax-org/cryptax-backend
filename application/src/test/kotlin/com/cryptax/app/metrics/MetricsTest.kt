package com.cryptax.app.metrics

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.config.objectMapper
import com.cryptax.app.verticle.RestVerticle
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import org.hamcrest.CoreMatchers.equalTo
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
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        val appConfig = TestAppConfig()
        RestAssured.port = appConfig.properties.server.port
        RestAssured.baseURI = "http://" + appConfig.properties.server.domain
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper })
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
            log().all().
            contentType(ContentType.JSON).
        get("/ping").
        then().
            log().all().
            assertThat().body("result", equalTo("pong")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }

    @Test
    @DisplayName("Get metrics from the server")
    fun testMetrics(testContext: VertxTestContext) {
        // @formatter:off
        given().
            log().all().
            contentType(ContentType.JSON).
            queryParam("key","cryptax.http.servers.0.0.0.0:8080.connections").
        get("/metrics").
        then().
            log().all().
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
            log().all().
            contentType(ContentType.JSON).
        get("/metrics/available").
        then().
            log().all().
            assertThat().body("[0]", equalTo("cryptax.http.servers.0.0.0.0:8080.connections")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
