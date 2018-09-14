package com.cryptax.app.route

import io.reactivex.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class InfoRoutes {

    private val info: Map<String, String> by lazy { loadInfoFile() }

    @GetMapping("/")
    fun root() {
    }

    @GetMapping("/info")
    fun info(): Single<Map<String, String>> {
        return Single.just(info)
    }

    private fun loadInfoFile(): Map<String, String> {
        val inputStream = InfoRoutes::class.java.getResourceAsStream("/META-INF/spring-boot-app.properties")
        return inputStream
            .bufferedReader()
            .readLines()
            .associateBy(
                keySelector = { line -> line.substring(startIndex = 0, endIndex = line.indexOf('=')) },
                valueTransform = { line -> line.substring(startIndex = line.indexOf('=') + 1, endIndex = line.length) })
    }
}
