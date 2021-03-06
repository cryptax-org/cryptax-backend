package com.cryptax.parser

import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import java.io.InputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class Parser(
    protected val utc: ZoneId = ZoneId.of("UTC"),
    protected val pattern: DateTimeFormatter,
    protected val source: Source,
    protected val delimiter: Char) {

    abstract fun parse(inputStream: InputStream, userId: String): List<Transaction>
}
