package com.cryptax.app.route

import com.cryptax.app.Application
import com.cryptax.app.route.Utils.createUser
import com.cryptax.app.route.Utils.setupRestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
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

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured(randomServerPort.toInt())
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
            assertThat().body("error", equalTo("Missing parameter [token]"))
        // @formatter:on
    }
}
