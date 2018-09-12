package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.createUser
import com.cryptax.app.route.Utils.getToken
import com.cryptax.app.route.Utils.initUserAndGetToken
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.app.route.Utils.validateToken
import com.cryptax.app.route.Utils.validateUser
import com.cryptax.db.InMemoryUserRepository
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DisplayName("Token routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TokenRoutesTest {

    @LocalServerPort
    lateinit var randomServerPort: String

    @Autowired
    lateinit var memory: InMemoryUserRepository

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured(randomServerPort.toInt())
    }

    @BeforeEach
    fun setUp() {
        memory.deleteAll()
    }

    @DisplayName("Obtain token")
    @Test
    fun testObtainToken() {
        // given
        val pair = createUser()
        validateUser(pair)

        // when
        val token = getToken()

        // then
        val actualToken = token.getString("token")
        val actualRefreshToken = token.getString("refreshToken")
        assertThat(actualToken).isNotNull()
        assertThat(actualRefreshToken).isNotNull()
        validateToken(actualToken, pair.first.id, false)
        validateToken(actualRefreshToken, pair.first.id, true)
    }

    @DisplayName("Obtain token, wrong credentials")
    @Test
    fun testObtainTokenWrongCredentials() {
        val pair = createUser()
        validateUser(pair)
        val credentials = JsonNodeFactory.instance.objectNode().put("email", pair.first.email).put("password", "mywrongpassword").toString()

        // @formatter:off
        given().
            log().ifValidationFails().
            body(credentials).
            contentType(ContentType.JSON).
        post("/token").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }

    @DisplayName("Obtain token from refresh token")
    @Test
    fun testObtainTokenFromRefreshToken() {
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("refreshToken")}")).
        get("/refresh").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("token", notNullValue()).
            assertThat().body("refreshToken", notNullValue())
        // @formatter:on
    }

    @DisplayName("Obtain token with wrong token")
    @Test
    fun getTokenRefreshTokenWithWrongToken() {
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/refresh").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }
}
