package com.cryptax.parser.utils

import com.cryptax.domain.entity.Currency
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class UtilsTest {

    @ParameterizedTest
    @MethodSource("marketProvider")
    fun testExtractCurrencies(market: String, expected: Pair<Currency, Currency>) {
        // when
        val actual = extractCurrencies(market)

        // then
        assertThat(expected).isEqualTo(actual)
    }

    @ParameterizedTest
    @ValueSource(strings = ["DERPDERP", "ETHPPPPPPPPP", "FHIKUWEHFKJ#EWHK", "", "1"])
    fun testExtractCurrencyFail(market: String) {
        assertThrows(RuntimeException::class.java) {
            extractCurrencies(market)
        }
    }

    companion object {

        @JvmStatic
        fun marketProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("ICXETH", Pair(Currency.ICON, Currency.ETH)),
                Arguments.of("XMRETH", Pair(Currency.MONERO, Currency.ETH)),
                Arguments.of("BQXETH", Pair(Currency.ETHOS, Currency.ETH)),
                Arguments.of("POWRETH", Pair(Currency.POWER_LEDGER, Currency.ETH)),
                Arguments.of("ETHPOWR", Pair(Currency.ETH, Currency.POWER_LEDGER)),
                Arguments.of("ADAETH", Pair(Currency.CARDANO, Currency.ETH))
            )
        }
    }
}
