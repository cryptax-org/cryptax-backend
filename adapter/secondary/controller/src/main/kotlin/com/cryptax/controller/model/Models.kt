package com.cryptax.controller.model

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Details
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Metadata
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import java.time.ZonedDateTime

data class UserWeb(
    val id: String = "DEFAULT",
    val email: String = "",
    val password: CharArray? = null,
    val lastName: String = "",
    val firstName: String = "") {

    fun toUser(): User {
        return User(
            id = id,
            email = email,
            password = password!!,
            lastName = lastName,
            firstName = firstName)
    }

    companion object {
        fun toUserWeb(user: User): UserWeb {
            return UserWeb(id = user.id,
                email = user.email,
                lastName = user.lastName,
                firstName = user.firstName)
        }
    }
}

data class TransactionWeb(
    val id: String = "DEFAULT",
    val source: String? = null,
    val date: ZonedDateTime? = null,
    val type: Transaction.Type? = null,
    val price: Double? = null,
    val quantity: Double? = null,
    val currency1: Currency? = null,
    val currency2: Currency? = null) {

    fun toTransaction(userId: String): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            source = source!!,
            date = date!!,
            type = type!!,
            price = price!!,
            quantity = quantity!!,
            currency1 = currency1!!,
            currency2 = currency2!!
        )
    }

    fun toTransaction(transactionId: String, userId: String): Transaction {
        return Transaction(
            id = transactionId,
            userId = userId,
            source = source!!,
            date = date!!,
            type = type!!,
            price = price!!,
            quantity = quantity!!,
            currency1 = currency1!!,
            currency2 = currency2!!
        )
    }

    companion object {

        fun toTransactionWeb(transaction: Transaction): TransactionWeb {
            return TransactionWeb(
                id = transaction.id,
                source = transaction.source,
                date = transaction.date,
                type = transaction.type,
                price = transaction.price,
                quantity = transaction.quantity,
                currency1 = transaction.currency1,
                currency2 = transaction.currency2
            )
        }
    }
}

data class ReportWeb(
    val totalCapitalGainShort: Double,
    val totalCapitalGainLong: Double,
    val breakdown: Map<String, DetailsWeb>) {
    companion object {
        fun toReportWeb(report: Report, debug: Boolean): ReportWeb {
            val breakdown: MutableMap<String, DetailsWeb> = mutableMapOf()
            report.breakdown.forEach {
                breakdown[it.key.code] = DetailsWeb.toLinesWeb(it.value, debug)
            }
            return ReportWeb(
                totalCapitalGainShort = report.totalCapitalGainShort,
                totalCapitalGainLong = report.totalCapitalGainLong,
                breakdown = breakdown
            )
        }
    }
}

data class DetailsWeb(val capitalGainShort: Double?, val capitalGainLong: Double?, val lines: List<LineWeb>) {
    companion object {
        fun toLinesWeb(details: Details, debug: Boolean): DetailsWeb {
            return DetailsWeb(details.capitalGainShort, details.capitalGainLong, details.lines.map { LineWeb.toLineWeb(it, debug) })
        }
    }
}

data class LineWeb(
    val transactionId: String,
    val date: ZonedDateTime,
    val currency1: Currency,
    val currency2: Currency,
    val type: Transaction.Type,
    val price: Double,
    val quantity: Double,
    var metadata: Metadata? = null) {
    companion object {
        fun toLineWeb(line: Line, debug: Boolean): LineWeb {
            return LineWeb(
                transactionId = line.transactionId,
                date = line.date,
                currency1 = line.currency1,
                currency2 = line.currency2,
                type = line.type,
                price = line.price,
                quantity = line.quantity,
                metadata = if (debug) line.metadata else null
            )
        }
    }
}

data class ResetPasswordWeb(val token: String)

data class CurrencyWeb(
    val code: String,
    val name: String,
    val symbol: String,
    val type: String
)
