package com.cryptax.email

data class EmailConfig(val enabled: Boolean,
                       private val baseUrl: String = "",
                       private val function: String = "",
                       private val key: String = "",
                       val from: String = "") {

    val url = "$baseUrl$function?sg_key=$key"
}
