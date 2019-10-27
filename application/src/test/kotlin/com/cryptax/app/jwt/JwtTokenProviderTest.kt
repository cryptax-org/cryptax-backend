package com.cryptax.app.jwt

import com.cryptax.config.JwtProps
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class JwtTokenProviderTest {

    private val jwtTokenProvider = JwtTokenProvider()

    @BeforeAll
    internal fun beforeAll() {
        jwtTokenProvider.profile = "test"
    }

    @Test
    fun `validate token illegal argument`() {
        // given
        val jwtProps = JwtProps("", "", "", "", 2, 2)
        jwtTokenProvider.jwtProps = jwtProps

        // when
        val actual = jwtTokenProvider.validateToken("fghkwfghwkej")

        // then
        assertThat(actual).isFalse()
    }

    @Test
    fun `validate token jwt exception`() {
        // given
        val jwtProps = JwtProps("", "password", "", "", 2, 2)
        jwtTokenProvider.jwtProps = jwtProps

        // when
        val actual = jwtTokenProvider.validateToken("eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJjODBiN2UxNi03YjY2LTQzYjAtOTRhNS02NzQyNGMwOWUyNTEiLCJzdWIiOiIzYTJlOWNmNmI4ODMxMWU4YTFjZjAyNDI5MDAyZGJiMSIsImlzcyI6IkNyeXB0YXgiLCJpc1JlZnJlc2giOmZhbHNlLCJhdXRoIjpbIlVTRVIiXSwiaWF0IjoxNTM2OTczNDQ1LCJleHAiOjE1MzY5NzUyNDV9.Lbqcr9-WObCUbA48sNWghDecQtOvrJ75_i4_FLR-qvvzofiunXZC1NCAI7TOSeKbAKuXPpjkoghr6ijm5qGMKg")

        // then
        assertThat(actual).isFalse()
    }
}
