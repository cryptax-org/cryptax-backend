package com.cryptax.app.exception

import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun validationException(ex: MethodArgumentNotValidException): ValidationErrorMessage {
        return ValidationErrorMessage(details = ex.bindingResult.allErrors.map { e -> e.defaultMessage ?: "Field validation issue" })
    }

    class ValidationErrorMessage(val error: String = "Invalid request", val details: List<String>)
}
