package com.cryptax.app.exception

import com.cryptax.app.jwt.JwtException
import com.cryptax.domain.exception.LoginException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException


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
        log.warn("Jwt issue")
    }

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun webExchangeBindException(ex: WebExchangeBindException): ValidationErrorMessage {
        return ValidationErrorMessage(details = ex.bindingResult.allErrors.map { e -> e.defaultMessage ?: "Field validation issue" })
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
