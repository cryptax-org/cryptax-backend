package com.cryptax.app.routes

import com.cryptax.app.routes.Routes.addContentTypeJson
import com.cryptax.domain.exception.CryptaxException
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import io.reactivex.exceptions.CompositeException
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException

object Failure {

    private val log = LoggerFactory.getLogger(Failure::class.java)

    val failureHandler: Handler<RoutingContext> = Handler { event ->
        val response = event.response().addContentTypeJson()

        if (event.statusCode() == 401) {
            val userId: String? = event.user()?.principal()?.getString("userId")
            log.warn("Unauthorized request for user [$userId] and route [${event.currentRoute().path}]")
            response
                .setStatusCode(401)
                .end(JsonObject().put("error", "Unauthorized").encodePrettily())
        } else {
            // The framework should guarantee that we have a failure
            val throwable: Throwable? = event.failure()
            when (throwable) {
                is ValidationException -> handleValidationException(response, throwable)
                is CompositeException -> handleRxException(response, throwable)
                is CryptaxException -> handleCryptaxException(response, throwable)
                null -> notHandledNoException(response)
                else -> notHandledException(response, throwable)
            }
        }
    }

    private fun notHandledNoException(response: HttpServerResponse) {
        log.error("Failure but no exception. Should the devs handle it?")
        response
            .setStatusCode(500)
            .end(JsonObject().put("error", "Something went wrong").encodePrettily())
    }

    private fun notHandledException(response: HttpServerResponse, throwable: Throwable) {
        log.error("Exception type [${throwable.javaClass.simpleName}] not handled. Should the devs handle it?", throwable)
        response
            .setStatusCode(500)
            .end(JsonObject().put("error", "Something went wrong").encodePrettily())
    }

    private fun handleRxException(response: HttpServerResponse, exception: CompositeException) {
        val cryptaxException = exception.exceptions.find { throwable -> throwable is CryptaxException }
        if (cryptaxException != null) {
            handleCryptaxException(response, cryptaxException as CryptaxException)
        } else {
            notHandledException(response, exception)
        }
    }

    private fun handleCryptaxException(response: HttpServerResponse, exception: CryptaxException) {
        when (exception) {
            is LoginException -> handleLoginException(response, exception)
            is UserNotFoundException -> handleUserNotFoundException(response, exception)
            is UserAlreadyExistsException -> handleUserUserAlreadyExistsException(response, exception)
            is UserValidationException -> handleUserTransactionValidationException(response, exception)
            is TransactionValidationException -> handleUserTransactionValidationException(response, exception)
            is TransactionNotFound -> handleTransactionNotFoundException(response, exception)
        }
    }

    private fun handleValidationException(response: HttpServerResponse, exception: ValidationException) {
        log.warn("Validation exception [${exception.message}]")
        response
            .setStatusCode(400)
            .end(JsonObject().put("error", "${exception.message}").encodePrettily())
    }

    private fun handleLoginException(response: HttpServerResponse, exception: LoginException) {
        log.warn("Unauthorized request for user [${exception.email}] and with description [${exception.description}]")
        response
            .setStatusCode(401)
            .end()
    }

    private fun handleUserNotFoundException(response: HttpServerResponse, exception: UserNotFoundException) {
        log.warn("User not found [${exception.message}]")
        response
            .setStatusCode(400)
            .end()
    }

    private fun handleUserUserAlreadyExistsException(response: HttpServerResponse, exception: UserAlreadyExistsException) {
        log.warn("User already exists [${exception.message}]")
        response
            .setStatusCode(400)
            .end()
    }

    private fun handleUserTransactionValidationException(response: HttpServerResponse, exception: com.cryptax.domain.exception.ValidationException) {
        log.debug("Validation exception [${exception.message}]")
        response
            .setStatusCode(400)
            .end(JsonObject().put("error", "${exception.message}").encodePrettily())
    }

    private fun handleTransactionNotFoundException(response: HttpServerResponse, exception: TransactionNotFound) {
        log.warn("Transaction not found [${exception.message}]")
        response
            .setStatusCode(404)
            .end()
    }
}
