package com.cryptax.parser

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.parser.model.BinanceTransaction
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

    override fun parse(inputStream: InputStream, userId: String): List<Transaction> {
        return CSVReaderBuilder(InputStreamReader(inputStream))
            .withSkipLines(1)
            .withKeepCarriageReturn(false)
            .withCSVParser(CSVParserBuilder().withSeparator(delimiter).build())
            .build()
            .readAll()
            .map { line ->
                BinanceTransaction(
                    date = LocalDateTime.parse(line[0], pattern).atZone(utc),
                    market = line[1],
                    type = line[2],
                    price = line[3].toDouble(),
                    amount = line[4].toDouble(),
                    total = line[5].toDouble(),
                    fee = line[6].toDouble(),
                    feeCoin = Currency.findCurrency(line[7]))
            }
            .map { it.toTransaction(userId) }
    }
}

