package com.cryptax.usecase.report.internal

import com.cryptax.domain.entity.Breakdown
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Report
import io.reactivex.Single
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("LinesToReport")

fun linesToReport(lines: List<Line>): Single<Report> {
    return Single.just(lines)
        .map { Breakdown(it) }
        .map { breakdown: Breakdown ->
            breakdown.compute()
            val totalCapitalGainShort = breakdown.keys
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .map { currency -> breakdown[currency]!! }
                .map { details -> details.capitalGainShort }
                .sum()

            val totalCapitalGainLong = breakdown.keys
                .filter { currency -> currency.type == Currency.Type.CRYPTO }
                .map { currency -> breakdown[currency]!! }
                .map { details -> details.capitalGainLong }
                .sum()

            Report(totalCapitalGainShort = totalCapitalGainShort, totalCapitalGainLong = totalCapitalGainLong, breakdown = breakdown)
        }
}
