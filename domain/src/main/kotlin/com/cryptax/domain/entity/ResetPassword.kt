package com.cryptax.domain.entity

import java.time.ZonedDateTime

data class ResetPassword(
    val userId: String,
    val token: String,
    val date: ZonedDateTime)
