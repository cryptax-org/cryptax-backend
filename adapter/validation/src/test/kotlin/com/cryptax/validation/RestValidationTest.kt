package com.cryptax.validation

import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@DisplayName("Request validation validation")
@ExtendWith(VertxExtension::class)
class RestValidationTest {

	private val host = "localhost"
	private val port = 8282

	@DisplayName("Check create user validation")
	@Test
	fun testCreateUserValidation(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val user = JsonObject().put("email", "email@email.com").put("password", "123").put("lastName", "john").put("firstName", "doe")
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.body = user.toBuffer()
				it.next()
			}
			.handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertNotEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}

	@DisplayName("Check create user validation, body null")
	@Test
	fun testCreateUserValidationBodyNull(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		router.route().handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}

	@DisplayName("Check create user validation, empty body")
	@Test
	fun testCreateUserValidationBodyEmpty(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.body = Buffer.buffer()
				it.next()
			}
			.handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}

	}

	@DisplayName("Check create user validation, empty json body")
	@Test
	fun testCreateUserValidationBodyEmptyJson(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.body = JsonObject().toBuffer()
				it.next()
			}
			.handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					vertx.createHttpClient().getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}

	}

	@DisplayName("Check create user validation, missing password")
	@Test
	fun testCreateUserValidationMissingPassword(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		val user = JsonObject().put("email", "email@email.com").put("lastName", "john").put("firstName", "doe")
		router.route()
			.handler {
				it.body = user.toBuffer()
				it.next()
			}
			.handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					vertx.createHttpClient().getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}

	@DisplayName("Check create user validation, wrong password type")
	@Test
	fun testCreateUserValidationWrongPasswordType(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val user = JsonObject().put("email", "email@email.com").put("password", 2).put("lastName", "john").put("firstName", "doe")
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.body = user.toBuffer()
				it.next()
			}
			.handler(createUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					vertx.createHttpClient().getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}

	@DisplayName("Check mandatory application/json")
	@Test
	fun testContentType(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.request().headers().add("Content-Type", "application/json")
				it.next()
			}
			.handler(jsonContentTypeValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					vertx.createHttpClient().getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertNotEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}

	@DisplayName("Check mandatory application/json fail")
	@Test
	fun testContentTypeFail(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val router = Router.router(vertx)
		router.route()
			.handler {
				it.request().headers().add("Content-Type", "fail")
				it.next()
			}
			.handler(jsonContentTypeValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					vertx.createHttpClient().getNow(port, host, "/") { resp ->
						// then
						testContext.verify {
							assertEquals(500, resp.statusCode())
							testContext.completeNow()
						}
					}
				} else {
					fail("The server did not start")
				}
			}
	}
}
