package com.cryptax.parser

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.parser.model.CoinbaseTransaction
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CoinbaseParser(delimiter: Char = ',') : Parser(
    pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    source = Source.COINBASE,
    delimiter = delimiter) {
    override fun parse(inputStream: InputStream, userId: String): List<Transaction> {
        return CSVReaderBuilder(InputStreamReader(inputStream))
            .withSkipLines(4)
            .withKeepCarriageReturn(false)
            .withCSVParser(CSVParserBuilder().withSeparator(delimiter).build())
            .build()
            .readAll()
            .map { line ->
                val test = LocalDate.parse(line[0], pattern).atStartOfDay().atZone(utc)
                CoinbaseTransaction(
                    date = test,
                    transactionType = line[1],
                    asset = Currency.findCurrency(line[2]),
                    quantity = line[3].toDouble(),
                    usdPrice = line[4].toDouble(),
                    usdAmount = line[5].toDouble())
            }
            .filter { coinbaseTransaction -> "buy" == coinbaseTransaction.transactionType.toLowerCase() || "sell" == coinbaseTransaction.transactionType.toLowerCase() }
            .map { it.toTransaction(userId) }
    }
}
