package com.cryptax.validation

import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler
import io.vertx.ext.web.api.validation.ParameterType

object RestValidation {

	// FIXME Add json body schema validation with schema http://json-schema.org/draft-04/schema#
	val createUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()

	// FIXME Add json body schema validation with schema http://json-schema.org/draft-04/schema#
	val addTransactionValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()

	val loginValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
		.create()
		.addQueryParam("email", ParameterType.EMAIL, true)
		.addQueryParam("password", ParameterType.GENERIC_STRING, true)

	val getUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
		.create()
		.addPathParam("userId", ParameterType.GENERIC_STRING)
}
