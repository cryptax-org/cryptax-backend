package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Transaction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class LinesToReportTest {

    @Test
    fun testGetOriginalPrice() {
        // given
        val coinsOwned: List<CoinsOwned> = listOf(
            CoinsOwned(
                date = ZonedDateTime.now(),
                price = 4403.09,
                quantity = 0.5)
        )
        val line = Line(transactionId = "id",
            date = ZonedDateTime.now(),
            currency1 = Currency.ETH,
            currency2 = Currency.BTC,
            type = Transaction.Type.BUY,
            price = 0.05828,
            quantity = 2.0,
            currency1UsdValue = 861.97,
            currency2UsdValue = 14754.13)

        // when
        val actual = getOriginalPrice(coinsOwned, line)

        // then
        assertThat(actual).isEqualTo(513.2241704)
        assertThat(coinsOwned[0].quantity).isEqualTo(0.38344)
    }
}
