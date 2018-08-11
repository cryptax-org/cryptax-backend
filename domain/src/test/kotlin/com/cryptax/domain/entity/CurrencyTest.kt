package com.cryptax.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CurrencyTest {

    @Test
    fun validateCurrencies() {
        // given
        val values = Currency.values()

        // when
        val actual = values.map { currency -> currency.code }.toSet()

        // then
        assertThat(actual).hasSize(values.size)
    }
}
