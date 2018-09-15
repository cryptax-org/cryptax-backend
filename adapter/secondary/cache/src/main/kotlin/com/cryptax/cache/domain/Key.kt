package com.cryptax.cache.domain

import com.cryptax.domain.entity.Currency
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.DataSerializable
import java.time.ZonedDateTime

class Key(private val currency: Currency, private val date: ZonedDateTime) : DataSerializable {

    override fun writeData(out: ObjectDataOutput) {
        out.writeUTF(currency.code)
        out.writeUTF(date.zone.id)
        out.writeLong(date.toInstant().toEpochMilli() / 1000)
    }

    override fun readData(input: ObjectDataInput) {
    }
}
