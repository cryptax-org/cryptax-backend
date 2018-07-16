package com.cryptax.parser

import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.parser.model.TransactionParser
import com.cryptax.parser.utils.extractCurrencies
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BinanceParser(delimiter: Char = ',') : Parser(
    pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    source = Source.BINANCE,
    delimiter = delimiter) {

    override fun parse(inputStream: InputStream): List<TransactionParser> {
        return CSVReaderBuilder(InputStreamReader(inputStream))
            .withSkipLines(1)
            .withKeepCarriageReturn(false)
            .withCSVParser(CSVParserBuilder().withSeparator(delimiter).build())
            .build()
            .readAll()
            .map { line ->
                val market = extractCurrencies(line[1])
                TransactionParser(
                    source = source,
                    date = LocalDateTime.parse(line[0], pattern).atZone(utc),
                    type = Transaction.Type.valueOf(line[2]),
                    price = line[3].toDouble(),
                    amount = line[4].toDouble(),
                    currency1 = market.first,
                    currency2 = market.second)
            }
    }
}

