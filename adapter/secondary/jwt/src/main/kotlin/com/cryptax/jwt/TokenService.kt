package com.cryptax.jwt

import com.cryptax.config.JwtProps
import com.cryptax.jwt.exception.JwtException
import com.cryptax.jwt.model.Role
import com.cryptax.jwt.model.Token
import com.cryptax.jwt.model.TokenDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.reactivex.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.UUID

interface TokenService {

    fun buildToken(userId: String): Single<Token>

    fun buildTokenFromRefresh(token: String): Single<Token>

    fun validateToken(token: String): Boolean

    fun tokenDetails(token: String): Single<TokenDetails>
}

class JwtService(private val jwtProps: JwtProps, private val profile: String) : TokenService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(JwtService::class.java)
    }

    override fun tokenDetails(token: String): Single<TokenDetails> {
        return Single.fromCallable {
            val claims: Jws<Claims> = Jwts.parser().setSigningKey(jwtProps.password(profile)).parseClaimsJws(token)
            val roles: List<Role> = (claims.body["auth"] as List<*>).map { str -> Role.valueOf(str as String) }
            TokenDetails(claims.body.subject, roles)
        }
    }

    override fun buildToken(userId: String): Single<Token> {
        return Single.fromCallable {
            val token = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            Token(userId, token, refreshToken)
        }
    }

    override fun validateToken(token: String): Boolean {
        val result = try {
            val jwt = Jwts.parser().setSigningKey(jwtProps.password(profile)).parseClaimsJws(token)
            !(jwt.body["isRefresh"] as Boolean)
        } catch (e: io.jsonwebtoken.JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            log.error("Issue while validating token $token", e)
            false
        }
        if (!result) log.warn("Expired or invalid JWT token: $token")
        return result
    }

    override fun buildTokenFromRefresh(token: String): Single<Token> {
        return Single.fromCallable {
            if (token.isBlank()) throw JwtException("Token not provided")
            val isRefresh = Jwts.parser().setSigningKey(jwtProps.password(profile)).parseClaimsJws(token).body["isRefresh"]
            if (isRefresh == false) throw JwtException("Refresh token expected")
            val userId = getUserId(token)
            val newToken = createToken(userId, listOf(Role.USER), false)
            val refreshToken = createToken(userId, listOf(Role.USER), true)
            Token(userId, newToken, refreshToken)
        }
    }

    private fun getUserId(token: String): String {
        return Jwts.parser().setSigningKey(jwtProps.password(profile)).parseClaimsJws(token).body.subject
    }

    private fun createToken(userId: String, roles: List<Role>, isRefresh: Boolean): String {
        val claims = Jwts.claims()
            .setId(UUID.randomUUID().toString())
            .setSubject(userId)
            .setIssuer(jwtProps.issuer)
        claims["isRefresh"] = isRefresh
        claims["auth"] = roles.map { role -> role.authority() }

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
            .signWith(signature, jwtProps.password(profile))
            .compact()
    }
}
