package com.cryptax.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CurrencyTest {

    @Test
    fun `validate currencies`() {
        // given
        val values = Currency.values()

        // when
        val actual = values.map { currency -> currency.code }.toSet()

        // then
        assertThat(actual).hasSize(values.size)
    }

    @Test
    fun `check if currencies have only one default`() {
        Currency.values().forEach { currency ->
            val actual = currency.codes.count { code -> code.default }

            assertThat(actual).`as`("Currency $currency").isEqualTo(1)
        }
    }
}
