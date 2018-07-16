package com.cryptax.parser

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.File
import java.io.FileInputStream
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
                val market = parseMarket(line[1])
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

    private fun parseMarket(market: String): Pair<Currency, Currency> {
        // FIXME to handle more cases
        if (market.length == 6) {
            return Pair(Currency.findCurrency(market.substring(0, 3)), Currency.findCurrency(market.substring(3, 6)))
        } else if (market.length == 7) {
            var currency1 = Currency.findCurrency(market.substring(0, 3))
            val currency2: Currency
            if (currency1 == Currency.UNKNOWN) {
                currency1 = Currency.findCurrency(market.substring(0, 4))
                currency2 = Currency.findCurrency(market.substring(4, 7))
            } else {
                currency2 = Currency.findCurrency(market.substring(3, 7))
            }
            return Pair(currency1, currency2)
        } else {
            return Pair(Currency.UNKNOWN, Currency.UNKNOWN)
        }
    }
}

fun main(args: Array<String>) {
    val initialFile = File("/home/carl/Downloads/Binance-Trade-History.csv")
    val targetStream = FileInputStream(initialFile)
    val derp = BinanceParser().parse(targetStream)
    targetStream.close()
    println("Res: $derp")

    val initialFile2 = File("/home/carl/Downloads/Binance-Trade-History2.csv")
    val targetStream2 = FileInputStream(initialFile2)
    val derp2 = BinanceParser(delimiter = ';').parse(targetStream2)
    targetStream2.close()
    println("Res: $derp2")
}

