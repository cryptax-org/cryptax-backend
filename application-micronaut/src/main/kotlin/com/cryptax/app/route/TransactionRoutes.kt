package com.cryptax.app.route

import com.cryptax.controller.TransactionController
import com.cryptax.controller.model.TransactionWeb
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.parameters.RequestBody
import reactor.adapter.rxjava.toMono
import reactor.core.publisher.Mono
import javax.validation.Valid

@Controller
class TransactionRoutes(private val transactionController: TransactionController) {

/*    @Post("/users/{userId}/transactions")
    fun addTransaction(
        @RequestBody @Valid transactionWeb: TransactionWeb,
        @PathVariable userId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.addTransaction(userId, transactionWeb).toMono() }
    }

    @Post("/users/{userId}/transactions/bulk")
    fun addMultipleTransactions(
        @RequestBody @Valid transactions: List<TransactionWeb>,
        @PathVariable userId: String): Mono<List<TransactionWeb>> {
        return verifyUserId(userId).flatMap { transactionController.addMultipleTransactions(userId, transactions).toMono() }
    }

    @Get("/users/{userId}/transactions")
    fun getAllTransactions(@PathVariable userId: String): Mono<List<TransactionWeb>> {
        return verifyUserId(userId).flatMap { transactionController.getAllTransactions(userId).toMono() }
    }

    @Get("/users/{userId}/transactions/{transactionId}")
    fun getOneTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.getTransaction(transactionId, userId).toMono() }
    }

    @Put("/users/{userId}/transactions/{transactionId}")
    fun updateTransaction(
        @RequestBody @Valid transactionWeb: TransactionWeb,
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.updateTransaction(transactionId, userId, transactionWeb).toMono() }
    }

    @Delete("/users/{userId}/transactions/{transactionId}")
    fun deleteTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<Unit> {
        return verifyUserId(userId).flatMap { transactionController.deleteTransaction(transactionId, userId).toMono() }
    }*/

/*    @Post("/users/{userId}/transactions/upload", consumes = [MediaType.MULTIPART_FORM_DATA])
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
