package com.cryptax.controller.validation

import com.cryptax.controller.model.UserWeb
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPassengerCountValidator::class])
@kotlin.annotation.MustBeDocumented
annotation class ValidUserWeb(
    val message: String = "Invalid user",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [])

class ValidPassengerCountValidator : ConstraintValidator<ValidUserWeb, UserWeb> {

    lateinit var groups: Array<KClass<*>>

    override fun initialize(constraintAnnotation: ValidUserWeb) {
        groups = constraintAnnotation.groups
    }

    override fun isValid(userWeb: UserWeb, context: ConstraintValidatorContext): Boolean {
        return userWeb.password != null
    }
}
