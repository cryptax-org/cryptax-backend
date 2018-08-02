package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
@DisplayName("Vertx cache test")
class VertxCacheServiceTest {

    private val cacheName = "cacheName"

    lateinit var vertx: Vertx
    @InjectMocks
    lateinit var vertxCacheService: VertxCacheService

    @BeforeEach
    fun beforeEach(testContext: VertxTestContext) {
        val options = VertxOptions().setClusterManager(HazelcastClusterManager())
        Vertx.clusteredVertx(options) { ar ->
            if (ar.succeeded()) {
                vertx = ar.result()
                vertxCacheService = VertxCacheService(vertx)
                testContext.completeNow()
            } else {
                testContext.failNow(RuntimeException("Vertx did not start"))
            }
        }
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @AfterEach
    fun afterEach(testContext: VertxTestContext) {
        vertx.close { ar ->
            if (ar.succeeded()) testContext.completeNow()
            else testContext.failNow(RuntimeException("Vertx did not stop"))
        }
    }

    @Test
    fun testGet(testContext: VertxTestContext) {
        // given
        val currency = Currency.ETH
        val date =  ZonedDateTime.now()

        // when
        val actual = vertxCacheService.get(cacheName, currency, date)

        // then
        assertThat(actual).isNull()
        testContext.completeNow()
    }

    @Test
    fun testPutGet(testContext: VertxTestContext) {
        // given
        val value = Pair("service", 10.0)
        val currency = Currency.ETH
        val date =  ZonedDateTime.now()

        // when
        vertxCacheService. put(cacheName, currency, date, value)
        val actual = vertxCacheService.get(cacheName, currency, date)

        // then
        assertThat(actual).isEqualTo(value)
        testContext.completeNow()
    }
}
