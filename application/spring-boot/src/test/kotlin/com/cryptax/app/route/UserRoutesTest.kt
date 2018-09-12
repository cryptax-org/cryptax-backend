package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.model.ResetPasswordRequest
import com.cryptax.app.route.Utils.createUser
import com.cryptax.app.route.Utils.initUserAndGetToken
import com.cryptax.app.route.Utils.initiatePasswordReset
import com.cryptax.app.route.Utils.setupRestAssured
import com.cryptax.app.route.Utils.user
import com.cryptax.db.InMemoryUserRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DisplayName("User routes integration tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UserRoutesTest {

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

    @DisplayName("Create a user")
    @Test
    fun testCreate() {
        createUser()
    }

    @DisplayName("Create a user, no body")
    @Test
    fun testCreateWithEmptyBody() {
        // @formatter:off
        given().
            log().ifValidationFails().
            body("{}").
            contentType(ContentType.JSON).
        post("/users").
        then().
        log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Invalid request")).
            assertThat().body("details", hasItems(
                                                        "Email can not be empty",
                                                        "Password can not be empty",
                                                        "Last name can not be empty",
                                                        "First name can not be empty"))
        // @formatter:on
    }

    @DisplayName("Allow user")
    @Test
    fun testAllowUser() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow?token=${pair.second}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @DisplayName("Allow user, missing token")
    @Test
    fun testAllowUserNoToken() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400).
            assertThat().body("error", equalTo("Required String parameter 'token' is not present"))
        // @formatter:on
    }

    @DisplayName("Allow user, missing token")
    @Test
    fun testAllowUserWrongToken() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
        get("/users/${pair.first.id}/allow?token=WrongToken").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @DisplayName("Get one user")
    @Test
    fun testGetOneUser() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/${token.getString("id")}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200).
            assertThat().body("id", equalTo(token.getString("id"))).
            assertThat().body("email", equalTo(user.email)).
            assertThat().body("password", nullValue()).
            assertThat().body("lastName", equalTo(user.lastName)).
            assertThat().body("firstName",equalTo(user.firstName))
        // @formatter:on
    }

    @DisplayName("Get one user, user do not exists")
    @Test
    fun testGetOneUserNotExists() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/users/WroNgId").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }

    @DisplayName("Get one user with refresh token")
    @Test
    fun testGetOneUserWithRefreshToken() {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("refreshToken")}")).
        get("/users/${token.getString("id")}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(401)
        // @formatter:on
    }

    @DisplayName("Send welcome email")
    @Test
    fun testSendWelcomeEmail() {
        // given
        val pair = createUser()

        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/${pair.first.email}").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @DisplayName("Send welcome email, user not found")
    @Test
    fun testSendWelcomeEmailUserNotFound() {
        // given
        val email = "whatever"

        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/$email").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @DisplayName("Initiate reset password")
    @Test
    fun testInitiatePasswordReset() {
        // given
        val pair = createUser()

        initiatePasswordReset(pair)
    }

    @DisplayName("Initiate reset password, user not found")
    @Test
    fun testInitiatePasswordResetNotFound() {
        // @formatter:off
        given().
            log().ifValidationFails().
        get("/users/email/email@email.com/reset").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }

    @DisplayName("Reset password")
    @Test
    fun testResetPassword() {
        val pair = createUser()
        val token = initiatePasswordReset(pair).getString("token")

        val resetPassword = ResetPasswordRequest(pair.first.email, "pass".toCharArray(), token)

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            body(resetPassword).
            log().ifValidationFails().
        put("/users/password").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)
        // @formatter:on
    }

    @DisplayName("Reset password, unknown user")
    @Test
    fun testResetPasswordUserNotFound() {
        val resetPassword = ResetPasswordRequest("email@email.com", "dwqdqd".toCharArray(), "token")

        // @formatter:off
        given().
            contentType(ContentType.JSON).
            body(resetPassword).
            log().ifValidationFails().
        put("/users/password").
        then().
            log().ifValidationFails().
            assertThat().statusCode(400)
        // @formatter:on
    }
}
