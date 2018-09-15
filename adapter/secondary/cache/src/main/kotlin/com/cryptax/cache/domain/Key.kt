package com.cryptax.cache.domain

import com.cryptax.domain.entity.Currency
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.DataSerializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Key() : DataSerializable {

    lateinit var currency: Currency
    lateinit var date: ZonedDateTime

    constructor(currency: Currency, date: ZonedDateTime) : this() {
        this.currency = currency
        this.date = date
    }

    override fun writeData(out: ObjectDataOutput) {
        out.writeUTF(currency.code)
        out.writeUTF(date.zone.id)
        out.writeLong(date.toInstant().toEpochMilli() / 1000)
    }

    override fun readData(input: ObjectDataInput) {
        currency = Currency.findCurrency(input.readUTF())
        val zoneId = input.readUTF()
        date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(input.readLong()), ZoneId.of(zoneId))
    }
}
