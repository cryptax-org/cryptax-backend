package com.cryptax.controller.exception

import com.cryptax.controller.validation.Violation

class ControllerValidationException(val errors: List<Violation>) : RuntimeException()
