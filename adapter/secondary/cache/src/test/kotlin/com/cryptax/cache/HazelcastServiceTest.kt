package com.cryptax.cache

import com.cryptax.domain.entity.Currency
import com.hazelcast.core.Hazelcast
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

@DisplayName("Hazelcast cache test")
class HazelcastServiceTest {

    private val hazelcastService = HazelcastService(Hazelcast.newHazelcastInstance())

    @Test
    fun `put item in cache and retrieve it`() {
        // given
        val cache = "cacheName"
        val currency = Currency.ETH
        val now = ZonedDateTime.now()

        // when
        hazelcastService.put(cache, currency, now, Pair("co", 10.4)).blockingGet()
        val actual = hazelcastService.get(cache, currency, now)

        // then
        assertThat(actual).isNotNull
    }

    @Test
    fun `retrieve something that does not exists`() {
        // given
        val cache = "cacheName"
        val currency = Currency.ETH
        val now = ZonedDateTime.now()

        // when
        val actual = hazelcastService.get(cache, currency, now).blockingGet()

        // then
        assertThat(actual).isNull()
    }
}
