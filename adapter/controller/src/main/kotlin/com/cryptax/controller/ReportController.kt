package com.cryptax.controller

//import com.cryptax.domain.entity.Report
import com.cryptax.controller.model.FinalReportWeb
import com.cryptax.usecase.report.GenerateReport
import io.reactivex.Single

class ReportController(private val generateReport: GenerateReport) {

    fun generateReport(userId: String, debug: Boolean): Single<FinalReportWeb> {
        return generateReport.generate(userId)
            .map { report -> FinalReportWeb.toReportWeb(report, debug) }
/*            .map { report ->
                if (debug) {
                    report.result.values.flatMap { it.lines }.forEach { line -> line.metadata = null }
                }
                report
            }
            .map { report -> ReportWeb.toReportWeb(report) }*/
    }
}
