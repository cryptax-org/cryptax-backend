package com.cryptax.db

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.ZoneId
import java.util.TimeZone

val objectMapper: ObjectMapper = ObjectMapper()
	.registerModule(KotlinModule())
	.registerModule(JavaTimeModule())
	.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
	.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
	.setSerializationInclusion(JsonInclude.Include.NON_NULL)
