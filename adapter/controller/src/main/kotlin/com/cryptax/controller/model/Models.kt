package com.cryptax.controller.model

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Line
//import com.cryptax.domain.entity.Report
//import com.cryptax.domain.entity.Result
import com.cryptax.domain.entity.Source
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import java.time.ZonedDateTime
import java.util.Arrays

data class UserWeb(
    val id: String? = null,
    val email: String,
    val password: CharArray? = null,
    val lastName: String,
    val firstName: String) {

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
        var result = id?.hashCode() ?: 0
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

data class TransactionWeb(
    val id: String? = null,
    val source: Source,
    val date: ZonedDateTime,
    val type: Transaction.Type,
    val price: Double,
    val quantity: Double,
    val currency1: Currency,
    val currency2: Currency
) {

    fun toTransaction(userId: String): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            source = source,
            date = date,
            type = type,
            price = price,
            quantity = quantity,
            currency1 = currency1,
            currency2 = currency2
        )
    }

    fun toTransaction(transactionId: String, userId: String): Transaction {
        return Transaction(
            id = transactionId,
            userId = userId,
            source = source,
            date = date,
            type = type,
            price = price,
            quantity = quantity,
            currency1 = currency1,
            currency2 = currency2
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

/*
data class ReportWeb(val pairs: Map<String, ResultWeb>) {

    companion object {
        fun toReportWeb(report: Report): ReportWeb {
            val lines = HashMap<String, ResultWeb>()
            report.pairs.entries.forEach { entry: Map.Entry<String, Result> ->
                lines[entry.key] = ResultWeb.toResultWeb(entry.value)
            }
            return ReportWeb(lines)
        }
    }
}

data class ResultWeb(val gainsLosses: Double, val lines: Set<LineWeb>) {
    companion object {
        fun toResultWeb(result: Result): ResultWeb {
            return ResultWeb(
                result.getGainsLosses(),
                result.lines.map { LineWeb.toLineWeb(it) }.toSet())
        }
    }
}

data class LineWeb(val currency1UsdValue: Double, val currency2UsdValue: Double, val transaction: TransactionWeb) {
    companion object {
        fun toLineWeb(line: Line): LineWeb {
            return LineWeb(
                currency1UsdValue = line.currency1UsdValue,
                currency2UsdValue = line.currency2UsdValue,
                transaction = TransactionWeb.toTransactionWeb(line.transaction))
        }
    }
}
*/
