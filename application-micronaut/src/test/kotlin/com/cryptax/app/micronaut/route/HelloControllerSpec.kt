package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.Application
import com.cryptax.controller.model.UserWeb
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class HelloControllerSpec {
    val objectMapper = ObjectMapper()
    val user: UserWeb = objectMapper.readValue(UserRoutesTest::class.java.getResourceAsStream("/user.json"), UserWeb::class.java)

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    lateinit var context: ApplicationContext

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun testHelloWorldResponse() {
        val rsp: String = client.toBlocking().retrieve("/hello")
        assertEquals("Hello World", rsp)
        assertThat(true).isTrue()
    }

    @Test
    fun testHelloWorldResponseRestAssured() {
        RestAssured.port = server.port
        given().
            log().all().
            get("/hello").
        then().
            log().ifValidationFails().
            assertThat().statusCode(200)//.
            //assertThat().body(`is`("Hello World"))
    }

/*    @Test
    fun testHelloWorldResponse2() {
        val request = HttpRequest.POST<Any>("/users", user)
        val rsp: String = client.toBlocking().retrieve(request, String::class.java)
        assertEquals("Hello World", rsp)
    }*/

/*    @Test
    fun testHelloWorldResponse3() {
        val request = HttpRequest.POST<Any>("/derp", "{\"email\":\"cp.harmant@protonmail.com\",\"password\":\"mypassword\",\"lastName\":\"Doe\",\"firstName\":\"John\"}")
        val rsp: String = client.toBlocking().retrieve(request, String::class.java)
        assertEquals("Hello World", rsp)
    }

    @Test
    fun testHelloWorldResponse5() {
        val request = HttpRequest.POST<Any>("/derp", user)
        val rsp: String = client.toBlocking().retrieve(request, String::class.java)
        assertEquals("Hello World", rsp)
    }*/
}
