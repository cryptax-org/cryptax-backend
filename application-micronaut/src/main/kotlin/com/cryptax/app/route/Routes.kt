package com.cryptax.app.route

import com.cryptax.app.model.ErrorResponse
import com.cryptax.controller.exception.ControllerValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

internal fun verifyUserId(userId: String): Mono<Boolean> {
/*    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { context ->
            when (context.authentication.principal as String) {
                userId -> Mono.just(true)
                else -> Mono.error(JwtException("User $userId can't be accessed with the given token ${context.authentication.credentials}"))
            }
        }*/
    return Mono.just(true)
}

private val log = LoggerFactory.getLogger(Routes::class.java)

private const val INVALID_REQUEST = "Invalid request"
private const val SOMETHING_WRONG = "Something went wrong"

interface Routes {

    @Error(global = true)
    fun error(request: HttpRequest<*>, throwable: Throwable): HttpResponse<*> {
        if (throwable is UserNotFoundException) {
            log.info("User not found [${throwable.message}]")
            return HttpResponse.unauthorized<Any>()
        }
        if (throwable is ControllerValidationException) {
            val details = throwable.errors.map { violation -> violation.message }
            val response = ErrorResponse(error = INVALID_REQUEST, details = details)
            return HttpResponse.badRequest<ErrorResponse>(response)
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
