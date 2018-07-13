package com.cryptax.security.encoder

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA3_512

interface Encoder {

    fun encode(str: String): String
}

class Sha3512Encoder : Encoder {

    private val digestUtils = DigestUtils(SHA3_512)

    override fun encode(str: String): String {
        return digestUtils.digestAsHex(str)
    }
}
