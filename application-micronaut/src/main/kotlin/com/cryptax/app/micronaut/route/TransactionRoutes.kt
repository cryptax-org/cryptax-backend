package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.security.SecurityContext
import com.cryptax.controller.TransactionController
import com.cryptax.controller.model.TransactionWeb
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.reactivex.Maybe
import io.reactivex.Single

@Controller("/users/{userId}")
open class TransactionRoutes(private val transactionController: TransactionController, private val securityContext: SecurityContext) {

    @Post("/transactions")
    fun addTransaction(
        @Body transactionWeb: TransactionWeb,
        @PathVariable userId: String): Single<TransactionWeb> {
        return securityContext.validateUserId(userId).flatMap { transactionController.addTransaction(userId, transactionWeb) }
    }

    @Post("/transactions/bulk")
    fun addMultipleTransactions(
        @Body transactions: List<TransactionWeb>,
        @PathVariable userId: String): Single<List<TransactionWeb>> {
        return securityContext.validateUserId(userId).flatMap { transactionController.addMultipleTransactions(userId, transactions) }
    }

    @Get("/transactions")
    fun getAllTransactions(@PathVariable userId: String): Single<List<TransactionWeb>> {
        return securityContext.validateUserId(userId).flatMap { transactionController.getAllTransactions(userId) }
    }

    @Get("/transactions/{transactionId}")
    fun getOneTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Maybe<TransactionWeb> {
        return securityContext.validateUserId(userId).toMaybe().flatMap { transactionController.getTransaction(transactionId, userId) }
    }

    @Put("/transactions/{transactionId}")
    fun updateTransaction(
        @Body transactionWeb: TransactionWeb,
        @PathVariable userId: String,
        @PathVariable transactionId: String): Single<TransactionWeb> {
        return securityContext.validateUserId(userId).flatMap { transactionController.updateTransaction(transactionId, userId, transactionWeb) }
    }

    @Delete("/transactions/{transactionId}")
    fun deleteTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Single<Unit> {
        return securityContext.validateUserId(userId).flatMap { transactionController.deleteTransaction(transactionId, userId) }
    }

/*    @Post("/transactions/upload", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadCsv(
        @RequestPart("file") file: Mono<FilePart>,
        @PathVariable userId: String,
        @RequestParam(value = "source", required = true) source: String,
        @RequestParam(value = "delimiter", required = false, defaultValue = ",") delimiter: Char): Mono<List<TransactionWeb>> {
        return verifyUserId(userId)
            .flatMap { file.timeout(Duration.ofMillis(200), Mono.error(ParamException())) }
            .flatMap { filePart ->
                filePart
                    .content()
                    .reduce(object : InputStream() {
                        override fun read() = -1
                    }) { inputStream: InputStream, dataBuffer: DataBuffer -> SequenceInputStream(inputStream, dataBuffer.asInputStream()) }
                    .flatMap { inputStream -> transactionController.uploadCSVTransactions(inputStream, userId, Source.valueOf(source.toUpperCase()), delimiter).toMono() }
            }
    }*/
}
