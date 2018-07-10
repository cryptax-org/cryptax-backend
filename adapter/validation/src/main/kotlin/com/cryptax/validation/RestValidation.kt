package com.cryptax.validation

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.impl.RequestParameterImpl
import io.vertx.ext.web.api.validation.CustomValidator
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler
import io.vertx.ext.web.api.validation.ParameterType
import io.vertx.ext.web.api.validation.ValidationException
import io.vertx.ext.web.api.validation.impl.HTTPRequestValidationHandlerImpl
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

object RestValidation {

	val createUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandlerCreateUser()
	val transactionBodyValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandlerTransaction()
		.addCustomValidatorFunction(userIdPathParamValidation)
	val getTransactionValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()
		.addCustomValidatorFunction(userIdPathParamValidation)

	val loginValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
		.create()
		.addQueryParam("email", ParameterType.EMAIL, true)
		.addQueryParam("password", ParameterType.GENERIC_STRING, true)

	val getUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
		.create()
		.addPathParam("userId", ParameterType.GENERIC_STRING)
		.addCustomValidatorFunction(userIdPathParamValidation)

	val jsonContentTypeValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()
		.addHeaderParamWithCustomTypeValidator("Content-Type", { value ->
			if (value == null || value != "application/json") {
				throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(value, "application/json")
			}
			RequestParameterImpl("Content-Type", value)

		}, true, false)
}

private val userIdPathParamValidation = CustomValidator { routingContext ->
	val userId = routingContext.request().getParam("userId")
	if (routingContext.user().principal().getString("id") != userId) {
		throw ValidationException("User [$userId] can't be accessed with the given token", ValidationException.ErrorType.NO_MATCH)
	}
}

private class HTTPRequestValidationHandlerCreateUser : HTTPRequestValidationHandlerCustom(listOf("email", "password", "lastName", "firstName")) {

	override fun checkBodyFieldsValueType(body: JsonObject) {
		mandatoryFields.forEach {
			if (body.getValue(it) !is String)
				throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [$it] should be a String")
		}
	}
}

private open class HTTPRequestValidationHandlerTransaction : HTTPRequestValidationHandlerCustom(listOf("source", "date", "type", "price", "amount", "currency1", "currency2")) {

	override fun checkBodyFieldsValueType(body: JsonObject) {
		val source = body.getValue("source")
		if (source !is String || source != "MANUAL") {
			throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [source] should be 'MANUAL'")
		}
		try {
			val date = body.getValue("date") as? String ?: throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [date] should be a String")
			ZonedDateTime.parse(date)
		} catch (e: DateTimeParseException) {
			throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [date] has a wrong format. It should be similar to 2011-12-03T10:15:30+01:00[Europe/Paris]")
		}

		val type = body.getValue("type")
		if (type !is String && (type != "SELL" || type != "BUY")) {
			throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [type] should be 'SELL' or 'BUY'")
		}
	}
}

private abstract class HTTPRequestValidationHandlerCustom(protected val mandatoryFields: List<String>) : HTTPRequestValidationHandlerImpl() {

	override fun handle(routingContext: RoutingContext) {
		try {
			if (routingContext.body == null || "" == routingContext.body.toString()) {
				throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Body is null or empty")
			}
			val json = routingContext.body.toJsonObject()
			if (json.isEmpty) throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Body is null or empty")


			checkBodyMandatoryFields(json)
			checkBodyFieldsValueType(json)

			super.handle(routingContext)

		} catch (e: ValidationException) {
			routingContext.fail(e)
		}
	}

	fun checkBodyMandatoryFields(body: JsonObject) {
		mandatoryFields.forEach {
			if (!body.containsKey(it))
				throw ValidationException.ValidationExceptionFactory.generateObjectFieldNotFound(it)
		}
	}

	abstract fun checkBodyFieldsValueType(body: JsonObject)
}
