package com.cryptax.validation

import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.loginValidation
import com.cryptax.validation.RestValidation.transactionBodyValidation
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.impl.JWTUser
import io.vertx.ext.web.Router
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * ☢☢ Those are not very reliable tests. This should be updated to assert the right failure
 */
@DisplayName("Request validation validation")
@ExtendWith(VertxExtension::class)
class RestValidationTest {

	private val host = "localhost"
	private val port = 8282

	@DisplayName("😀 Check create user validation")
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

	@DisplayName("☹ Check create user validation, body null")
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

	@DisplayName("☹ Check create user validation, empty body")
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

	@DisplayName("☹ Check create user validation, empty json body")
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

	@DisplayName("☹ Check create user validation, missing password")
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

	@DisplayName("☹ Check create user validation, wrong password type")
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

	@DisplayName("😀 Check mandatory application/json")
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

	@DisplayName("☹ Check mandatory application/json wrong type")
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

	@DisplayName("😀 Check transaction body validation")
	@Test
	fun testTransactionBodyValidation(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val transaction = JsonObject()
			.put("source", "MANUAL")
			.put("date", "2011-12-03T10:15:30Z")
			.put("type", "BUY")
			.put("price", 10.0)
			.put("amount", 5.0)
			.put("currency1", "BTC")
			.put("currency2", "ETH")
		val router = Router.router(vertx)
		router.get("/users/:userId/transactions")
			.handler {
				it.body = transaction.toBuffer()
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(transactionBodyValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/users/$userId/transactions") { resp ->
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

	@DisplayName("☹ Check transaction body validation wrong source")
	@Test
	fun testTransactionBodyValidationWrongSource(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val transaction = JsonObject()
			.put("source", "MANUAL2")
			.put("date", "2011-12-03T10:15:30Z")
			.put("type", "BUY")
			.put("price", 10.0)
			.put("amount", 5.0)
			.put("currency1", "BTC")
			.put("currency2", "ETH")
		val router = Router.router(vertx)
		router.get("/users/:userId/transactions")
			.handler {
				it.body = transaction.toBuffer()
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(transactionBodyValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/users/$userId/transactions") { resp ->
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

	@DisplayName("☹ Check transaction body validation wrong date format")
	@Test
	fun testTransactionBodyValidationWrongDateFormat(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val transaction = JsonObject()
			.put("source", "MANUAL")
			.put("date", "2011-12-03")
			.put("type", "BUY")
			.put("price", 10.0)
			.put("amount", 5.0)
			.put("currency1", "BTC")
			.put("currency2", "ETH")
		val router = Router.router(vertx)
		router.get("/users/:userId/transactions")
			.handler {
				it.body = transaction.toBuffer()
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(transactionBodyValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/users/$userId/transactions") { resp ->
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

	@DisplayName("☹ Check transaction body validation wrong type")
	@Test
	fun testTransactionBodyValidationWrongType(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val transaction = JsonObject()
			.put("source", "MANUAL")
			.put("date", "2011-12-03T10:15:30Z")
			.put("type", "BUYe")
			.put("price", 10.0)
			.put("amount", 5.0)
			.put("currency1", "BTC")
			.put("currency2", "ETH")
		val router = Router.router(vertx)
		router.get("/users/:userId/transactions")
			.handler {
				it.body = transaction.toBuffer()
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(transactionBodyValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/users/$userId/transactions") { resp ->
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

	@DisplayName("☹ Check transaction body validation wrong type 2")
	@Test
	fun testTransactionBodyValidationWrongType2(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val transaction = JsonObject()
			.put("source", "MANUAL")
			.put("date", "2011-12-03T10:15:30Z")
			.put("type", 2)
			.put("price", 10.0)
			.put("amount", 5.0)
			.put("currency1", "BTC")
			.put("currency2", "ETH")
		val router = Router.router(vertx)
		router.get("/users/:userId/transactions")
			.handler {
				it.body = transaction.toBuffer()
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(transactionBodyValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/users/$userId/transactions") { resp ->
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

	@DisplayName("😀 Check login validation")
	@Test
	fun testUserLoginValidation(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val loginUser = JsonObject()
			.put("email", "email@email.com")
			.put("password", "eqweqe")
		val router = Router.router(vertx)
		router.get("/")
			.handler {
				it.body = loginUser.toBuffer()
				it.next()
			}
			.handler(loginValidation)

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

	@DisplayName("☹ Check login validation email missing")
	@Test
	fun testUserLoginValidationEmailMissing(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val loginUser = JsonObject()
			.put("password", "eqweqe")
		val router = Router.router(vertx)
		router.get("/")
			.handler {
				it.body = loginUser.toBuffer()
				it.next()
			}
			.handler(loginValidation)

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

	@DisplayName("☹ Check login validation password missing")
	@Test
	fun testUserLoginValidationPasswordMissing(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val loginUser = JsonObject().put("email", "email@email.com")
		val router = Router.router(vertx)
		router.get("/")
			.handler {
				it.body = loginUser.toBuffer()
				it.next()
			}
			.handler(loginValidation)

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

	@DisplayName("😀 Get user validation")
	@Test
	fun testGetUserValidation(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val router = Router.router(vertx)
		router.get("/user/:userId")
			.handler {
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(getUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/user/$userId") { resp ->
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

	@DisplayName("☹ Get user validation")
	@Test
	fun testGetUserValidationWrongUser(vertx: Vertx, testContext: VertxTestContext) {
		// given
		val userId = "randomId"
		val router = Router.router(vertx)
		router.get("/user/:userId")
			.handler {
				it.setUser(JWTUser(JsonObject().put("id", userId), ""))
				it.next()
			}
			.handler(getUserValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port) { res ->
				if (res.succeeded()) {
					// when
					val client = vertx.createHttpClient()
					client.getNow(port, host, "/user/otherUser") { resp ->
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
