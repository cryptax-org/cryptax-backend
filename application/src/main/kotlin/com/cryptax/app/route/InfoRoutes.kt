package com.cryptax.app.route

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.SortedMap

@RestController
class InfoRoutes @Autowired constructor(private val healthCheckRegistry: HealthCheckRegistry) {

    private val info: Map<String, String> by lazy { loadInfoFile() }

    @GetMapping("/")
    fun root() {
    }

    @GetMapping("/info")
    fun info(): Single<Map<String, String>> {
        return Single.just(info)
    }

    @GetMapping("/health")
    fun health(): Single<SortedMap<String, HealthCheck.Result>> {
        return Single.fromCallable { healthCheckRegistry.runHealthChecks() }
    }

    @GetMapping("/ping")
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
