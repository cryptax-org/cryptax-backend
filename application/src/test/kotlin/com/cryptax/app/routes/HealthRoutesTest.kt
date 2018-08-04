package com.cryptax.app.routes

import com.cryptax.app.config.TestAppConfig
import com.cryptax.app.config.kodein
import com.cryptax.app.setupRestAssured
import com.cryptax.app.verticle.RestVerticle
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("User routes integration tests")
class HealthRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured()
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestAppConfig(), kodein()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Get health")
    fun testHealth(testContext: VertxTestContext) {
        // @formatter:off
         given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/health").
        then().
            log().ifValidationFails().
            assertThat().body("transactionRepository.healthy", equalTo(true)).
            assertThat().body("transactionRepository", hasKey("timestamp")).
            assertThat().body("userRepository.healthy", equalTo(true)).
            assertThat().body("userRepository", hasKey("timestamp")).
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}
