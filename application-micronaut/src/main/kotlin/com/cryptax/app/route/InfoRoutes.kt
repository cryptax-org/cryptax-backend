package com.cryptax.app.route

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import java.util.SortedMap

@Controller
class InfoRoutes(private val healthCheckRegistry: HealthCheckRegistry) {

    private val info: Map<String, String> by lazy { loadInfoFile() }

    @Get("/")
    fun root() {
    }

    @Get("/info")
    fun info(): Single<Map<String, String>> {
        return Single.just(info)
    }

    @Get("/health")
    fun health(): Single<SortedMap<String, HealthCheck.Result>> {
        return Single.fromCallable { healthCheckRegistry.runHealthChecks() }
    }

    @Get("/ping")
    fun ping(): Single<JsonNode> {
        return Single.just(JsonNodeFactory.instance.objectNode().put("result", "pong"))
    }

    private fun loadInfoFile(): Map<String, String> {
        val inputStream = InfoRoutes::class.java.getResourceAsStream("/META-INF/application.properties")
        return inputStream
            .bufferedReader()
            .readLines()
            .associateBy(
                keySelector = { line -> line.substring(startIndex = 0, endIndex = line.indexOf('=')) },
                valueTransform = { line -> line.substring(startIndex = line.indexOf('=') + 1, endIndex = line.length) })
    }
}
