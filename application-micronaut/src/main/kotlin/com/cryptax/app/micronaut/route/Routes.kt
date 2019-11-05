package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.model.ErrorResponse
import com.cryptax.app.micronaut.security.SecurityContextException
import com.cryptax.controller.validation.ValidationException
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.ResetPasswordException
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.jwt.exception.JwtException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import io.reactivex.Single
import org.slf4j.LoggerFactory

/*import com.cryptax.app.model.ErrorResponse
import com.cryptax.controller.exception.ControllerValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.hazelcast.spi.impl.operationservice.impl.responses.ErrorResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono*/

internal fun verifyUserId(userId: String): Single<Boolean> {
/*    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { context ->
            when (context.authentication.principal as String) {
                userId -> Mono.just(true)
                else -> Mono.error(JwtException("User $userId can't be accessed with the given token ${context.authentication.credentials}"))
            }
        }*/
    return Single.just(true)
}

private val log = LoggerFactory.getLogger(Routes::class.java)

private const val INVALID_REQUEST = "Invalid request"
private const val SOMETHING_WRONG = "Something went wrong"

interface Routes {

    @Error(global = true)
    fun error(request: HttpRequest<*>, throwable: Throwable): HttpResponse<*> {
        if (throwable is UserNotFoundException || throwable is ResetPasswordException) {
            log.info("User not found: ${throwable.message}")
            return HttpResponse.badRequest<Any>()
        }
        if (throwable is TransactionValidationException) {
            val error = ErrorResponse(throwable.message!!)
            return HttpResponse.badRequest<ErrorResponse>(error)
        }
        if (throwable is JwtException) {
            return HttpResponse.unauthorized<Any>()
        }
        if (throwable is LoginException) {
            return HttpResponse.unauthorized<Any>()
        }
        if (throwable is ValidationException) {
            val response = ErrorResponse(INVALID_REQUEST, throwable.errors)
            return HttpResponse.badRequest<ErrorResponse>(response)
        }
        if (throwable is SecurityContextException) {
            log.info("Security issue: ${throwable.message}")
            return HttpResponse.unauthorized<Any>()
        }
        if (throwable is TransactionNotFound) {
            return HttpResponse.notFound<Any>()
        }
        if (throwable is UserAlreadyExistsException) {
            log.info("User already exists [${throwable.message}]")
            return HttpResponse.badRequest<Any>()
        }
        if (throwable is UnsatisfiedRouteException) {
            log.warn("Issue with route", throwable)
            val errorResponse = ErrorResponse(error = INVALID_REQUEST, details = listOf(throwable.message!!))
            return HttpResponse.badRequest<ErrorResponse>().body(errorResponse)
        }
        log.error("Unrecoverable exception", throwable)
        val response = ErrorResponse(SOMETHING_WRONG)
        return HttpResponse.serverError<ErrorResponse>().body(response)
    }
}

