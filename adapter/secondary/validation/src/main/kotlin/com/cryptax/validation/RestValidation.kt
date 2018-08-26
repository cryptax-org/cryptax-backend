package com.cryptax.validation

import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
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
    val deleteTransactionValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()
        .addCustomValidatorFunction(userIdPathParamValidation)

    val loginValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
        .create()
        .addCustomValidatorFunction(loginBodyValidation)

    val getUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
        .create()
        .addPathParam("userId", ParameterType.GENERIC_STRING)
        .addCustomValidatorFunction(userIdPathParamValidation)

    val allowUserValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
        .create()
        .addPathParam("userId", ParameterType.GENERIC_STRING)
        .addQueryParam("token", ParameterType.GENERIC_STRING, true)

    val uploadCsvValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler
        .create()
        .addPathParam("userId", ParameterType.GENERIC_STRING)
        .addQueryParam("source", ParameterType.GENERIC_STRING, true)
        .addQueryParam("delimiter", ParameterType.GENERIC_STRING, false)
        .addCustomValidatorFunction(userIdPathParamValidation)
        .addCustomValidatorFunction { routingContext ->
            val source = routingContext.request().getParam("source")
            try {
                Source.valueOf(source.toUpperCase())
            } catch (e: Exception) {
                throw ValidationException("Invalid source [$source]")
            }
            val delimiterParam = routingContext.request().getParam("delimiter")
            if (delimiterParam != null) {
                if (delimiterParam.length != 1) {
                    throw ValidationException("Invalid delimiter [$delimiterParam]")
                }
                val delimiter = delimiterParam.toCharArray()[0]
                if (delimiter != ',' && delimiter != ';') {
                    throw ValidationException("Invalid delimiter [$delimiterParam]")
                }
            }
        }

    val generateReport: HTTPRequestValidationHandler = HTTPRequestValidationHandler
        .create()
        .addCustomValidatorFunction(userIdPathParamValidation)

    val jsonContentTypeValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()
        .addHeaderParamWithCustomTypeValidator("Content-Type", { value ->
            if (!value.contains("application/json")) {
                throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(value, "application/json")
            }
            RequestParameterImpl("Content-Type", value)

        }, true, false)

    val csvContentTypeValidation: HTTPRequestValidationHandler = HTTPRequestValidationHandler.create()
        .addHeaderParamWithCustomTypeValidator("Content-Type", { value ->
            if (!value.contains("text/csv")) {
                throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(value, "text/csv")
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

private val loginBodyValidation = CustomValidator { routingContext ->
    if (!routingContext.bodyAsJson.containsKey("email")) throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [email] missing")
    if (!routingContext.bodyAsJson.containsKey("password")) throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [password] missing")
}

private class HTTPRequestValidationHandlerCreateUser : HTTPRequestValidationHandlerCustom(listOf("email", "password", "lastName", "firstName")) {

    override fun checkBodyFieldsValueType(body: JsonObject) {
        mandatoryFields.forEach {
            if (body.getValue(it) !is String)
                throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [$it] should be a String")
        }
    }
}

private open class HTTPRequestValidationHandlerTransaction : HTTPRequestValidationHandlerCustom(listOf("source", "date", "type", "price", "quantity", "currency1", "currency2")) {

    override fun checkBodyFieldsValueType(body: JsonObject) {
        body.getValue("source") as? String ?: throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [source] should be a String")
        try {
            val date = body.getValue("date") as? String ?: throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [date] should be a String")
            ZonedDateTime.parse(date)
        } catch (e: DateTimeParseException) {
            throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [date] has a wrong format. It should be similar to 2011-12-03T10:15:30+01:00[Europe/Paris]")
        }

        val type = body.getValue("type") as? String ?: throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [type] should be a String")
        if (type != Transaction.Type.BUY.toString().toLowerCase() && type != Transaction.Type.SELL.toString().toLowerCase()) {
            throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException("Object field [type] should be '${Transaction.Type.BUY.toString().toLowerCase()}' or '${Transaction.Type.SELL.toString().toLowerCase()}'")
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
