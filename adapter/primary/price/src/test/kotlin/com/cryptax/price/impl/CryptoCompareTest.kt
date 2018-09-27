package com.cryptax.price.impl

import com.cryptax.domain.entity.Currency
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime

@DisplayName("Cryptocompare test")
@ExtendWith(MockitoExtension::class)
class CryptoCompareTest {

    @Mock
    lateinit var call: Call
    @Mock
    lateinit var client: OkHttpClient
    @InjectMocks
    lateinit var cryptoCompare: CryptoCompare

    @BeforeEach
    fun setUp() {
        cryptoCompare = CryptoCompare(client = client, objectMapper = ObjectMapper())
    }

    @Test
    fun testFindUsdPriceAt() {
        // given
        val currency = Currency.ETH
        val body = "{\"${currency.code}\":{\"USD\":15.0}}"

        val response = Response.Builder()
            .request(Request.Builder().url("https://google.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(ResponseBody.create(MediaType.get("application/json"), body))
            .build()
        val timestamp = ZonedDateTime.now().toInstant().toEpochMilli() / 1000
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        val actual = cryptoCompare.findUsdPriceAt(currency, timestamp).blockingGet()

        // then
        assertThat(actual).isEqualTo(Pair("cryptoCompare", 15.0))
        argumentCaptor<Request>().apply {
            then(client).should().newCall(capture())
            assertThat(firstValue.url().toString()).isEqualTo("https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=USD&ts=$timestamp")
        }
    }

    @Test
    fun testFindUsdPriceAtBodyNull() {
        // given
        val response = Response.Builder()
            .request(Request.Builder().url("https://google.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .build()
        val timestamp = ZonedDateTime.now().toInstant().toEpochMilli() / 1000
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        val exception = assertThrows<RuntimeException> {
            cryptoCompare.findUsdPriceAt(Currency.ETH, timestamp).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo("The body received was null")
    }

    @Test
    fun testFindUsdPriceAtWrongBodyFormat() {
        // given
        val currency = Currency.ETH
        val body = "{\"derp\":\"derp\"}"

        val response = Response.Builder()
            .request(Request.Builder().url("https://google.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(ResponseBody.create(MediaType.get("application/json"), body))
            .build()
        val timestamp = ZonedDateTime.now().toInstant().toEpochMilli() / 1000
        given(client.newCall(any())).willReturn(call)
        given(call.execute()).willReturn(response)

        // when
        val exception = assertThrows<RuntimeException> {
            cryptoCompare.findUsdPriceAt(currency, timestamp).blockingGet()
        }

        // then
        assertThat(exception.message).isEqualTo("The body received does not have the right format {\"derp\":\"derp\"}")
    }
}
