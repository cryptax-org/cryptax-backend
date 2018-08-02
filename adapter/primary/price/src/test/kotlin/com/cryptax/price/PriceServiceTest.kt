package com.cryptax.price

import com.cryptax.cache.CacheService
import com.cryptax.domain.entity.Currency
import com.fasterxml.jackson.databind.ObjectMapper
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
    fun setUp() {
        priceService = PriceService(
            client = client,
            objectMapper = ObjectMapper(),
            cache = cache,
            api = api)
    }

    @Test
    fun testCurrencyUsdValue() {
        // given
        val date = ZonedDateTime.now()
        val timestamp = date.toInstant().toEpochMilli() / 1000
        val currency = Currency.ETH
        val expected = Pair("serviceName", 10.0)
        given(cache.get(cacheName, currency, date)).willReturn(null)
        given(api.findUsdPriceAt(currency, timestamp)).willReturn(expected)

        // when
        val actual = priceService.currencyUsdValueAt(currency, date)

        // then
        assertThat(actual).isEqualTo(expected)
        then(cache).should().get(cacheName, currency, date)
        then(cache).should().put(cacheName, currency, date, expected)
        then(api).should().findUsdPriceAt(currency, timestamp)
    }

    @Test
    fun testCurrencyUsdValueAtFromCache() {
        // given
        val date = ZonedDateTime.now()
        val currency = Currency.ETH
        val expected = Pair("serviceName", 10.0)
        given(cache.get(cacheName, currency, date)).willReturn(expected)

        // when
        val actual = priceService.currencyUsdValueAt(currency, date)

        // then
        assertThat(actual).isEqualTo(expected)
        then(cache).should().get(cacheName, currency, date)
        then(cache).shouldHaveNoMoreInteractions()
        then(api).shouldHaveZeroInteractions()
    }
}
