package com.cryptax.controller

import com.cryptax.domain.entity.FinalReport
//import com.cryptax.domain.entity.Report
import com.cryptax.usecase.report.GenerateReport
import io.reactivex.Single

class ReportController(private val generateReport: GenerateReport) {

    fun generateReport(userId: String): Single<FinalReport> {
        return generateReport.generate(userId)
            //.map { report -> ReportWeb.toReportWeb(report) }
    }
}
