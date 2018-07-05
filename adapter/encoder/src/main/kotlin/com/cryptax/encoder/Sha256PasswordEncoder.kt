package com.cryptax.encoder

import com.cryptax.domain.port.PasswordEncoder
import org.apache.commons.codec.digest.DigestUtils

class Sha256PasswordEncoder : PasswordEncoder {

	override fun encode(str: String): String {
		return DigestUtils.sha256Hex(str)
	}
}
