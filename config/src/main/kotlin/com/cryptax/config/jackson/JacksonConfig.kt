package com.cryptax.config.jackson

import com.cryptax.domain.entity.Currency
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.ZoneId
import java.util.TimeZone

object JacksonConfig {
    val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())
        .registerModule(CurrencyModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

private class CurrencySerializer : JsonSerializer<Currency>() {
    override fun serialize(value: Currency, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeString(value.code)
    }
}

private class CurrencyDeserializer : JsonDeserializer<Currency>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Currency {
        return Currency.findCurrency(p!!.getValueAsString(""))
    }
}

private class CurrencyModule : SimpleModule(NAME) {
    init {
        addSerializer(Currency::class.java, CurrencySerializer())
        addDeserializer(Currency::class.java, CurrencyDeserializer())
    }

    companion object {
        private const val NAME = "CurrencyModule"
    }
}
