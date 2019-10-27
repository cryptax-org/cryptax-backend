package com.cryptax.app.config

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
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
import io.micronaut.context.annotation.Factory
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@Factory
class JacksonConfig {

    @Singleton
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(JavaTimeModule())
            .registerModule(EnumModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    private class EnumModule : SimpleModule(NAME) {
        init {
            addSerializer(Currency::class.java, CurrencySerializer())
            addSerializer(Source::class.java, SourceSerializer())
            addSerializer(Transaction.Type::class.java, TransactionTypeSerializer())

            addDeserializer(Currency::class.java, CurrencyDeserializer())
            addDeserializer(Source::class.java, SourceDeserializer())
            addDeserializer(Transaction.Type::class.java, TransactionTypeDeserializer())
        }

        companion object {
            private const val NAME = "EnumModule"
        }
    }

    private class CurrencySerializer : JsonSerializer<Currency>() {
        override fun serialize(value: Currency, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.code)
        }
    }

    private class CurrencyDeserializer : JsonDeserializer<Currency>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Currency {
            return Currency.findCurrency(p.getValueAsString("").toUpperCase())
        }
    }

    private class SourceSerializer : JsonSerializer<Source>() {
        override fun serialize(value: Source, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.toString().toLowerCase())
        }
    }

    private class SourceDeserializer : JsonDeserializer<Source>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Source {
            return Source.valueOf(p.getValueAsString("").toUpperCase())
        }
    }

    private class TransactionTypeSerializer : JsonSerializer<Transaction.Type>() {
        override fun serialize(value: Transaction.Type, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.toString().toLowerCase())
        }
    }

    private class TransactionTypeDeserializer : JsonDeserializer<Transaction.Type>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Transaction.Type {
            return Transaction.Type.valueOf(p.getValueAsString("").toUpperCase())
        }
    }
}


