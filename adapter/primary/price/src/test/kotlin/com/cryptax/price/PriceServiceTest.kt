package com.cryptax.price

import com.cryptax.cache.CacheService
import com.cryptax.domain.entity.Currency
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime

@DisplayName("Price service tests")
@ExtendWith(MockitoExtension::class)
class PriceServiceTest {

    private val cacheName = "cache.currency"

    @Mock
    lateinit var client: OkHttpClient
    @Mock
    lateinit var cache: CacheService
    @Mock
    lateinit var api: CryptoApi
    lateinit var priceService: PriceService

    @BeforeEach
    internal fun `before each`() {
        priceService = PriceService(
            client = client,
            objectMapper = ObjectMapper(),
            cache = cache,
            api = api)
    }

    @Test
    fun `currency USD value`() {
        // given
        val date = ZonedDateTime.now()
        val timestamp = date.toInstant().toEpochMilli() / 1000
        val currency = Currency.ETH
        val expected = Pair("serviceName", 10.0)
        given(cache.get(cacheName, currency, date)).willReturn(Maybe.empty())
        given(api.findUsdPriceAt(currency, timestamp)).willReturn(Single.just(expected))
        given(cache.put(cacheName, currency, date, expected)).willReturn(Single.just(Unit))

        // when
        val actual = priceService.currencyUsdValueAt(currency, date).blockingGet()

        // then
        assertThat(actual).isEqualTo(expected)
        then(cache).should().get(cacheName, currency, date)
        then(cache).should().put(cacheName, currency, date, expected)
        then(api).should().findUsdPriceAt(currency, timestamp)
    }

    @Test
    fun `currency USD value at, from cache`() {
        // given
        val date = ZonedDateTime.now()
        val currency = Currency.ETH
        val expected = Pair("serviceName", 10.0)
        given(cache.get(cacheName, currency, date)).willReturn(Maybe.just(expected))

        // when
        val actual = priceService.currencyUsdValueAt(currency, date).blockingGet()

        // then
        assertThat(actual).isEqualTo(expected)
        then(cache).should().get(cacheName, currency, date)
        then(cache).shouldHaveNoMoreInteractions()
        then(api).shouldHaveZeroInteractions()
    }
}
