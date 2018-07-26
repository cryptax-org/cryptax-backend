package com.cryptax.controller

import com.cryptax.controller.model.ReportWeb
import com.cryptax.usecase.report.GenerateReport
import io.reactivex.Single

class ReportController(private val generateReport: GenerateReport) {

    fun generateReport(userId: String): Single<ReportWeb> {
        return generateReport.generate(userId)
            .map { report -> ReportWeb.toReportWeb(report) }
    }
}
