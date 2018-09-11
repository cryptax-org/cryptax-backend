package com.cryptax.app.jwt

import com.cryptax.config.JwtProps
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider {

    companion object {
        val log: Logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    }

    @Autowired
    lateinit var jwtProps: JwtProps

    fun getUserAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val claims: Jws<Claims> = Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(token)
        val roles = (claims.body["auth"] as List<*>).map { str -> SimpleGrantedAuthority(str as String) }
        return UsernamePasswordAuthenticationToken(claims.body.subject, token, roles)
    }

    fun resolveToken(req: ServerHttpRequest): String {
        val authHeader = req.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: ""
        return if (authHeader.isNotBlank() && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        } else ""
    }

    fun validateToken(token: String): Boolean {
        val result = try {
            val jwt = Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(token)
            !(jwt.body["isRefresh"] as Boolean)
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            log.error("Issue while validating token $token", e)
            false
        }
        if (!result) log.warn("Expired or invalid JWT token: $token")
        return result
    }

    fun buildToken(userId: String): Single<Triple<String, String, String>> {
        return Single.create { emitter ->
            val token = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            emitter.onSuccess(Triple(userId, token, refreshToken))
        }
    }

    fun buildTokenFromRefresh(req: ServerHttpRequest): Single<Triple<String, String, String>> {
        return Single.create { emitter ->
            val currentToken = resolveToken(req)
            if(currentToken.isBlank()) throw com.cryptax.app.jwt.JwtException("Token not provided")
            val isRefresh = Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(currentToken).body["isRefresh"]
            if (isRefresh == false) throw com.cryptax.app.jwt.JwtException("Refresh token expected")
            val userId = getUserId(currentToken)
            val token = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            emitter.onSuccess(Triple(userId, token, refreshToken))
        }
    }

    private fun getUserId(token: String): String {
        return Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(token).body.subject
    }

    private fun createToken(userId: String, roles: List<Role>, isRefresh: Boolean): String {
        val claims = Jwts.claims()
            .setId(UUID.randomUUID().toString())
            .setSubject(userId)
            .setIssuer(jwtProps.issuer)
        claims["isRefresh"] = isRefresh
        claims["auth"] = roles.map { role -> role.authority }

        val now = Date()
        val validityMilliseconds =
            if (isRefresh)
                now.time + jwtProps.refreshExpiresInDays * 86_400_000
            else
                now.time + jwtProps.expiresInMinutes * 60_000
        val validity = Date(validityMilliseconds)

        val signature = SignatureAlgorithm.valueOf(jwtProps.algorithm)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(signature, jwtProps.password())
            .compact()
    }
}
