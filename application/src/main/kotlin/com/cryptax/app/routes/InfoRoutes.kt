package com.cryptax.app.routes

import com.cryptax.app.Main
import com.cryptax.app.routes.Routes.sendSuccess
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

private val version: String by lazy { currentVersion() }
private val createdAt: String by lazy { createdAt() }

fun handleInfoRoutes(router: Router) {
    router.get("/derp")
        .handler { routingContext ->
            val info = JsonObject().put("version", version).put("createdAt", createdAt)
            sendSuccess(info, routingContext.response())
        }
        .failureHandler(Failure.failureHandler)

    router.get("/info")
        .handler { routingContext ->
            val info = JsonObject().put("version", version).put("createdAt", createdAt)
            sendSuccess(info, routingContext.response())
        }
        .failureHandler(Failure.failureHandler)
}

private fun currentVersion(): String {
    return loadInfoFile().getOrDefault("version", "Unknown")
}

private fun createdAt(): String {
    return loadInfoFile().getOrDefault("createdAt", "Unknown")
}

private fun loadInfoFile(): Map<String, String> {
    val inputStream = Main::class.java.getResourceAsStream("/info.properties")
    return inputStream
        .bufferedReader()
        .readLines()
        .associateBy(
            keySelector = { line -> line.substring(startIndex = 0, endIndex = line.indexOf('=')) },
            valueTransform = { line -> line.substring(startIndex = line.indexOf('=') + 1, endIndex = line.length) })
}
