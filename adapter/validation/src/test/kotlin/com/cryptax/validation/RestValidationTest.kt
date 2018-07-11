package com.cryptax.validation

import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("👋 Rest validation")
@ExtendWith(VertxExtension::class)
class RestValidationTest {

	private val host = "localhost"
	private val port = 8282
	lateinit var router: Router

	@DisplayName("Check mandatory application/json")
	@Test
	fun testContentType(vertx: Vertx, testContext: VertxTestContext) {
		// given
		router = Router.router(vertx)
		router.route()
			.handler {
				it.request().headers().add("Content-Type", "application/json")
				it.next()
			}
			.handler(jsonContentTypeValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port)

		val client = vertx.createHttpClient()

		// when
		client.getNow(port, host, "/") { resp ->

			// then
			testContext.verify {
				assertNotEquals(500, resp.statusCode())
				testContext.completeNow()
			}
		}
	}

	@DisplayName("Check mandatory application/json fail")
	@Test
	fun testContentTypeFail(vertx: Vertx, testContext: VertxTestContext) {
		// given
		router = Router.router(vertx)
		router.route()
			.handler {
				it.request().headers().add("Content-Type", "fail")
				it.next()
			}
			.handler(jsonContentTypeValidation)

		vertx.createHttpServer()
			.requestHandler { router.accept(it) }
			.listen(port)

		val client = vertx.createHttpClient()

		// when
		client.getNow(port, host, "/") { resp ->

			// then
			testContext.verify {
				assertEquals(500, resp.statusCode())
				testContext.completeNow()
			}
		}
	}
}
