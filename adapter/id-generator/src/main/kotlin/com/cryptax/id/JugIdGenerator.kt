package com.cryptax.id

import com.cryptax.domain.port.IdGenerator
import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.NoArgGenerator

class JugIdGenerator : IdGenerator {

	override fun generate(): String {
		return generator().generate().toString().replace("-".toRegex(), "")
	}

	private fun generator(): NoArgGenerator {
		return Generators.timeBasedGenerator(EthernetAddress.fromInterface())
	}
}
