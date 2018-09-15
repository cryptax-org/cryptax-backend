package com.cryptax.cache.domain

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.DataSerializable

class Value() : DataSerializable {

    lateinit var service: String
    var value: Double = 0.0

    constructor(service: String, value: Double) : this() {
        this.service = service
        this.value = value
    }

    override fun writeData(out: ObjectDataOutput) {
        out.writeUTF(service)
        out.writeDouble(value)
    }

    override fun readData(input: ObjectDataInput) {
        service = input.readUTF()
        value = input.readDouble()
    }
}
