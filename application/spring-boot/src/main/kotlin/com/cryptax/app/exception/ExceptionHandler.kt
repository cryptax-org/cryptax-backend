package com.cryptax.app.exception

import com.cryptax.app.jwt.JwtException
import com.cryptax.domain.exception.LoginException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus


@ControllerAdvice
class ExceptionHandler { //: ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun validationException(ex: MethodArgumentNotValidException): ValidationErrorMessage {
        return ValidationErrorMessage(details = ex.bindingResult.allErrors.map { e -> e.defaultMessage ?: "Field validation issue" })
    }

    @ExceptionHandler(LoginException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun loginException(exception: LoginException) {
        log.warn("Unauthorized request for user [${exception.email}] and with description [${exception.description}]")
    }

    @ExceptionHandler(JwtException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun jwtException(exception: JwtException) {
        log.warn("Jwt issue", exception)
    }

/*    @ExceptionHandler(UnsatisfiedServletRequestParameterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptionHandler(e: UnsatisfiedServletRequestParameterException): DefaultErrorMessage {
        log.warn("Missing parameter ${e.paramConditions.map { it }}")
        return DefaultErrorMessage("Missing parameter ${e.paramConditions.map { it }}")
    }*/

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptionHandler(e: Exception): DefaultErrorMessage {
        log.error("Unexpected exception", e)
        return DefaultErrorMessage("Unhandled exception ${e.javaClass.simpleName}")
    }

    class DefaultErrorMessage(val error: String)
    class ValidationErrorMessage(val error: String = "Invalid request", val details: List<String>)
}
