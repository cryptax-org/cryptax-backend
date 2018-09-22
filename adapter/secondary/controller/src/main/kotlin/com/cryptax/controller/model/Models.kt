package com.cryptax.controller.model

import com.cryptax.controller.validation.Create
import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Details
import com.cryptax.domain.entity.Line
import com.cryptax.domain.entity.Metadata
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import java.time.ZonedDateTime
import java.util.Arrays
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class UserWeb(
    val id: String = "DEFAULT",
    @get:NotEmpty(groups = [Create::class], message = "{user.email}")
    val email: String = "",
    @get:NotEmpty(groups = [Create::class], message = "{user.password}")
    val password: CharArray? = null,
    @get:NotEmpty(groups = [Create::class], message = "{user.lastname}")
    val lastName: String = "",
    @get:NotEmpty(groups = [Create::class], message = "{user.firstname}")
    val firstName: String = "") {

    fun toUser(): User {
        return User(
            id = id,
            email = email,
            password = password!!,
            lastName = lastName,
            firstName = firstName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserWeb

        if (id != other.id) return false
        if (email != other.email) return false
        if (!Arrays.equals(password, other.password)) return false
        if (lastName != other.lastName) return false
        if (firstName != other.firstName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (password?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + lastName.hashCode()
        result = 31 * result + firstName.hashCode()
        return result
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

class TransactionWeb() {

    constructor(id: String?, source: String, date: ZonedDateTime, type: Transaction.Type, price: Double, quantity: Double, currency1: Currency, currency2: Currency) : this() {
        if (id != null) {
            this.id = id
        }
        this.source = source
        this.date = date
        this.type = type
        this.price = price
        this.quantity = quantity
        this.currency1 = currency1
        this.currency2 = currency2
    }

    var id: String = "DEFAULT"
    @get:NotEmpty(message = "{transaction.source}")
    var source: String? = null
    @get:NotNull(message = "{transaction.date}")
    var date: ZonedDateTime? = null
    @get:NotNull(message = "{transaction.type}")
    var type: Transaction.Type? = null
    @get:NotNull(message = "{transaction.price}")
    var price: Double? = null
    @get:NotNull(message = "{transaction.quantity}")
    var quantity: Double? = null
    @get:NotNull(message = "{transaction.currency1}")
    var currency1: Currency? = null
    @get:NotNull(message = "{transaction.currency2}")
    var currency2: Currency? = null

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
