package com.cryptax.app.micronaut.web

import io.micronaut.http.HttpRequest

fun extractToken(currentRequest: HttpRequest<*>?): String {
    if (currentRequest == null) return ""
    val authHeader: String? = currentRequest.headers.authorization?.orElseGet { "" }
    if (authHeader != null && authHeader.isNotBlank() && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7)
    }
    return ""
}
