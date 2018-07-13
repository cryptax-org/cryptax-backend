package com.cryptax.app

import com.cryptax.config.Config
import com.cryptax.config.DefaultConfig
import com.cryptax.domain.entity.User
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
@DisplayName("Integration tests on basic flow")
class UserRoutesTest {

	lateinit var vertx: Vertx

	@BeforeEach
	fun beforeEach() {
		vertx = Vertx.vertx(VertxOptions()
			.setMaxEventLoopExecuteTime(1000)
			.setPreferNativeTransport(true)
			.setFileResolverCachingEnabled(true))
	}

	@AfterEach
	fun afterEach() {
		vertx.close()
	}

	@Test
	@DisplayName("Create a user")
	fun createUser(testContext: VertxTestContext) {
		// given
		val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
		val client = WebClient.create(vertx)

		// when
		vertx.deployVerticle(RestApplication(DefaultConfig()), testContext.succeeding { _ ->
			client.post(8080, "localhost", "/users").sendJson(JsonObject.mapFrom(user)) { ar ->
				// then
				testContext.verify {
					assert(ar.succeeded()) { "Something went wrong while handling the request" }
					assertEquals(200, ar.result().statusCode()) { "Wrong status in the response" }
					val body = ar.result().bodyAsJsonObject()
					assertNotNull(body.getString("id")) { "id is null" }
					assertEquals(user.email, body.getString("email")) { "email do not match" }
					assertNull(body.getString("password")) { "password do not match" }
					assertEquals(user.lastName, body.getString("lastName")) { "lastName do not match" }
					assertEquals(user.firstName, body.getString("firstName")) { "firstName do not match" }
				}
				testContext.completeNow()
			}
		})
	}
}
