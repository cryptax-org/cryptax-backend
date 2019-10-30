package com.cryptax.app.jwt

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest

fun extractToken(req: ServerHttpRequest): String {
    val authHeader = req.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: ""
    return if (authHeader.isNotBlank() && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7)
    } else ""
}
