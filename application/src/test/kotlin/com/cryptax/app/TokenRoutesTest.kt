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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
@DisplayName("Integration tests on basic flow")
class TokenRoutesTest {

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
	@DisplayName("Get a token")
	fun getToken(testContext: VertxTestContext) {
		// given
		val user = Config.objectMapper.readValue(this::class.java.getResourceAsStream("/User1.json"), User::class.java)
		val token = JsonObject().put("email", user.email).put("password", user.password.joinToString(""))
		val client = WebClient.create(vertx)

		// when
		vertx.deployVerticle(RestApplication(DefaultConfig()), testContext.succeeding { _ ->
			client.post(8080, "localhost", "/users").sendJson(JsonObject.mapFrom(user)) { _ ->
				client.post(8080, "localhost", "/token").sendJson(token) { ar ->
					// then
					testContext.verify {
						assert(ar.succeeded()) { "Something went wrong while handling the request" }
						assertEquals(200, ar.result().statusCode()) { "Wrong status in the response" }
						val body = ar.result().bodyAsJsonObject()
						assertNotNull(body.getString("id")) { "id is null" }
						assertNotNull(body.getString("token")) { "token is null" }
					}
					testContext.completeNow()
				}
			}
		})
	}
}
