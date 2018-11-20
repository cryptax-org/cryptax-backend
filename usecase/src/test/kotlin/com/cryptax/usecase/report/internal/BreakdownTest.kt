package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.exception.ReportException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

class BreakdownTest {

    @Test
    fun `test details`() {
        // given
        val lines = listOf(Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.SELL, 15.0, 2.0))
        val breakdown = Breakdown(lines)

        // when
        val actual = breakdown.details(Currency.ETH)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.lines).hasSize(1)
    }

    @Test
    fun `test details, fails`() {
        // given
        val lines = listOf(Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.SELL, 15.0, 2.0))
        val breakdown = Breakdown(lines)

        // when
        val actual = assertThrows<ReportException> {
            breakdown.details(Currency.BTC)
        }

        // then
        assertThat(actual.message).isEqualTo("Could not find [BTC]")
    }

    @Test
    fun `compute test fails`() {
        // given
        val line1 = Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.SELL, 15.0, 2.0)
        line1.metadata.ignored = false
        line1.metadata.currency1UsdValue = 1.0
        line1.metadata.currency2UsdValue = 2.0
        val line2 = Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.BUY, 15.0, 1.0)
        line2.metadata.ignored = false
        line2.metadata.currency1UsdValue = 1.0
        line2.metadata.currency2UsdValue = 2.0
        val lines = listOf(line1, line2)
        val breakdown = Breakdown(lines)

        // when
        val actual = assertThrows<ReportException> {
            breakdown.compute()
        }

        // then
        assertThat(actual.message).startsWith("Not enough coins:")
    }

    @Test
    fun `line to compute`() {
        // given
        val line1 = Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.BUY, 15.0, 5.0)
        val line2 = Line("", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.SELL, 15.0, 2.0)
        val lines = listOf(line1, line2)
        val breakdown = Breakdown(lines)

        // when
        val actual = breakdown.linesToCompute

        // then
        assertThat(actual).hasSize(1)
    }

    @Test
    fun `line to compute 2`() {
        // given
        val line1 = Line("id1", ZonedDateTime.now(), Currency.ETH, Currency.USD, Transaction.Type.BUY, 15.0, 5.0)
        val line2 = Line("id2", ZonedDateTime.now(), Currency.USD, Currency.ETH, Transaction.Type.BUY, 15.0, 2.0)
        val lines = listOf(line1, line2)
        val breakdown = Breakdown(lines)

        // when
        val actual = breakdown.linesToCompute

        // then
        assertThat(actual).hasSize(1)
    }
}
