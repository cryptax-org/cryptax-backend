package com.cryptax.app.routes

import com.cryptax.app.Main
import com.cryptax.app.routes.Routes.sendSuccess
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

private val info: Map<String, String> by lazy { loadInfoFile() }

fun handleInfoRoutes(router: Router) {
    router.get("/info")
        .handler { routingContext -> sendSuccess(JsonObject(info), routingContext.response()) }
        .failureHandler(Failure.failureHandler)
}

private fun loadInfoFile(): Map<String, String> {
    val inputStream = Main::class.java.getResourceAsStream("/META-INF/application.properties")
    return inputStream
        .bufferedReader()
        .readLines()
        .associateBy(
            keySelector = { line -> line.substring(startIndex = 0, endIndex = line.indexOf('=')) },
            valueTransform = { line -> line.substring(startIndex = line.indexOf('=') + 1, endIndex = line.length) })
}
