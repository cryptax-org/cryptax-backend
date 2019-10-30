package com.cryptax.app.route

import com.cryptax.controller.ReportController
import com.cryptax.controller.model.ReportWeb
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.adapter.rxjava.toMono

@RestController
class ReportRoutes @Autowired constructor(private val reportController: ReportController) {

    @GetMapping("/users/{userId}/report")
    fun generateReport(
        @PathVariable userId: String,
        @RequestParam(value = "debug", required = false, defaultValue = "false") debug: Boolean): Mono<ReportWeb> {
        return verifyUserId(userId).flatMap { reportController.generateReport(userId, debug).toMono() }
    }
}
