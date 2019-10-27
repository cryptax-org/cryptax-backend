package com.cryptax.app.exception

import com.cryptax.controller.exception.ControllerValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Produces
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.http.server.exceptions.ExceptionHandler
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)

@Produces
@Singleton
@Requirements(Requires(classes = [HttpStatusException::class, ExceptionHandler::class]))
class Derp : ExceptionHandler<HttpStatusException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>?, exception: HttpStatusException?): HttpResponse<*> {
        return HttpResponse.badRequest<Any>()
    }
}

@Error(global = true)
fun error(request: HttpRequest<*>, e: Throwable): HttpResponse<JsonError> {
    val error = JsonError("Bad Things Happened: " + e.message).link(Link.SELF, Link.of(request.uri))

    return HttpResponse.serverError<JsonError>()
        .body(error)
}

@Error(status = HttpStatus.BAD_REQUEST)
fun notFound(request: HttpRequest<*>): HttpResponse<JsonError> {
    val error = JsonError("Person Not Found").link(Link.SELF, Link.of(request.uri))

    return HttpResponse.notFound<JsonError>().body(error)
}

@Produces
@Singleton
@Requirements(Requires(classes = [Throwable::class, ExceptionHandler::class]))
class UnsatisfiedRouteExceptionHandler : ExceptionHandler<Throwable, HttpResponse<Any>> {
    override fun handle(request: HttpRequest<*>, exception: Throwable): HttpResponse<Any> {
        log.error("Not recoverable", exception)
        return HttpResponse.serverError<Any>()
    }
}

@Produces
@Singleton
@Requirements(Requires(classes = [ControllerValidationException::class, ExceptionHandler::class]))
class ValidationExceptionHandler : ExceptionHandler<ControllerValidationException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exceptionController: ControllerValidationException): HttpResponse<*> {
        val error = ValidationErrorMessage(details = exceptionController.errors.map { violation -> violation.message })
        return HttpResponse.badRequest<ValidationErrorMessage>(error)
    }
}

@Produces
@Singleton
@Requirements(Requires(classes = [UserAlreadyExistsException::class, ExceptionHandler::class]))
class UserAlreadyExistsExceptionHandler : ExceptionHandler<UserAlreadyExistsException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: UserAlreadyExistsException): HttpResponse<*> {
        log.warn("User already exists [${exception.message}]")
        return HttpResponse.badRequest<Any>()
    }
}

class DefaultErrorMessage(val error: String)
class ValidationErrorMessage(val error: String = "Invalid request", val details: List<String>)

/*
import com.cryptax.app.jwt.JwtException
import com.cryptax.domain.exception.LoginException
import com.cryptax.domain.exception.ResetPasswordException
import com.cryptax.domain.exception.TransactionNotFound
import com.cryptax.domain.exception.TransactionValidationException
import com.cryptax.domain.exception.UserAlreadyExistsException
import com.cryptax.domain.exception.UserNotFoundException
import com.cryptax.domain.exception.UserValidationException
import com.cryptax.domain.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

@ControllerAdvice
class ExceptionHandler {

    private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(LoginException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun loginException(exception: LoginException) {
        log.warn("Unauthorized request for user [${exception.email}] and with description [${exception.description}]")
    }

    @ExceptionHandler(JwtException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun jwtException(exception: JwtException) {
        log.warn("Jwt issue - ${exception.message}")
    }

    @ExceptionHandler(ServerWebInputException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun serverWebInputException(ex: ServerWebInputException): DefaultErrorMessage {
        return DefaultErrorMessage(ex.reason ?: "Input issue")
    }

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun webExchangeBindException(ex: WebExchangeBindException): ValidationErrorMessage {
        return ValidationErrorMessage(details = ex.bindingResult.allErrors.map { e -> e.defaultMessage ?: "Field validation issue" })
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun userNotFoundException(ex: UserNotFoundException) {
        log.warn("User not found [${ex.message}]")
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun userAlreadyExistsException(ex: UserAlreadyExistsException) {
        log.warn("User already exists [${ex.message}]")
    }

    @ExceptionHandler(ResetPasswordException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun resetPasswordException(ex: ResetPasswordException) {
        log.warn("Invalid reset password token [${ex.message}]")
    }

    @ExceptionHandler(TransactionNotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun transactionNotFoundException(exception: TransactionNotFound) {
        log.warn("Transaction not found [${exception.message}]")
    }

    @ExceptionHandler(value = [UserValidationException::class, TransactionValidationException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun validationException(exception: ValidationException): DefaultErrorMessage {
        log.debug("Validation exception [${exception.message}]")
        return DefaultErrorMessage("${exception.message}")
    }

    @ExceptionHandler(ParamException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun paramException(exception: ParamException): ValidationErrorMessage {
        log.debug("Validation exception [${exception.message}]")
        return ValidationErrorMessage(details = listOf("Csv file is mandatory"))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptionHandler(e: Exception): DefaultErrorMessage {
        log.warn("Unexpected exception", e)
        return DefaultErrorMessage("Unhandled exception ${e.javaClass.simpleName}")
    }

    class DefaultErrorMessage(val error: String)
    class ValidationErrorMessage(val error: String = "Invalid request", val details: List<String>)
}
*/
