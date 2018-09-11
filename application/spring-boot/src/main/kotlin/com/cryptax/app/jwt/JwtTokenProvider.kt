package com.cryptax.app.jwt

import com.cryptax.config.JwtProps
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import java.util.Objects
import java.util.UUID

//import javax.servlet.http.HttpServletRequest

@Component
class JwtTokenProvider {

    @Autowired
    lateinit var jwtProps: JwtProps

    fun getAuthentication(token: String): Authentication {
        val userDetails = org.springframework.security.core.userdetails.User
            .withUsername(getUserId(token))
            .password("")
            .authorities(Role.USER) // TODO: This should be extracted from the token
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build()
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    private fun getUserId(token: String): String {
        return Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(token).body.subject
    }

    fun resolveToken(req: ServerHttpRequest): String {
        val bearerToken: List<String> = req.headers.getValuesAsList("Authorization")
        return if (bearerToken.isNotEmpty() && bearerToken[0].startsWith("Bearer ")) {
            bearerToken[0].substring(7, bearerToken[0].length)
        } else ""
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(token)
            return true
        } catch (e: JwtException) {
            throw com.cryptax.app.jwt.JwtException("Expired or invalid JWT token", e)
        } catch (e: IllegalArgumentException) {
            throw com.cryptax.app.jwt.JwtException("Expired or invalid JWT token", e)
        }
    }

    fun buildToken(userId: String): Single<Triple<String, String, String>> {
        return Single.create { emitter ->
            val token = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            emitter.onSuccess(Triple(userId, token, refreshToken))
        }
    }

    fun buildTokenFromRefresh(req: ServerHttpRequest?): Single<Triple<String, String, String>> {
        return Single.create { emitter ->
            val currentToken = resolveToken(req!!)
            val isRefresh = Jwts.parser().setSigningKey(jwtProps.password()).parseClaimsJws(currentToken).body["isRefresh"]
            if (isRefresh == false) {
                throw com.cryptax.app.jwt.JwtException("Refresh token expected")
            }
            val userId = getUserId(currentToken)
            val token = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            emitter.onSuccess(Triple(userId, token, refreshToken))
        }
    }

    private fun createToken(userId: String, roles: List<Role>, isRefresh: Boolean): String {
        val claims = Jwts.claims()
            .setId(UUID.randomUUID().toString())
            .setSubject(userId)
            .setIssuer(jwtProps.issuer)
        claims["isRefresh"] = isRefresh
        claims["auth"] = roles
            .map { role -> SimpleGrantedAuthority(role.authority) }
            .filter { Objects.nonNull(it) }

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
