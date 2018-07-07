package com.cryptax.security.encoder

import org.apache.commons.codec.digest.DigestUtils

interface Encoder {
	fun encode(str: String): String
}

class Sha256Encoder : Encoder {

	override fun encode(str: String): String {
		return DigestUtils.sha256Hex(str)
	}
}
