package com.cryptax.app.micronaut.route

import com.cryptax.app.micronaut.security.SecurityContext
import com.cryptax.controller.TransactionController
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.validation.ValidationException
import com.cryptax.domain.entity.Source
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.multipart.StreamingFileUpload
import io.reactivex.Maybe
import io.reactivex.Single
import org.reactivestreams.Publisher
import java.io.File
import java.io.FileInputStream

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
        return securityContext.validateUserId(userId)
            .toMaybe()
            .flatMap { transactionController.getTransaction(transactionId, userId) }
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

    @Post("/transactions/upload", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadCsv(
        file: StreamingFileUpload?,
        @PathVariable userId: String,
        @QueryValue("source") source: String,
        @QueryValue(value = "delimiter", defaultValue = ",") delimiter: Char): Single<List<TransactionWeb>> {
        if(file == null) throw ValidationException(listOf("Csv file is mandatory"))
        val tempFile: File = File.createTempFile(file.filename, "temp")
        val uploadPublisher: Publisher<Boolean> = file.transferTo(tempFile)
        return securityContext.validateUserId(userId)
            .flatMap { Single.fromPublisher(uploadPublisher) }
            .map { success -> if (success) tempFile else throw RuntimeException() }
            .flatMap { currentFile ->
                val inputStream = FileInputStream(currentFile)
                transactionController.uploadCSVTransactions(inputStream, userId, Source.valueOf(source.toUpperCase()), delimiter)
                    .doAfterSuccess { inputStream.close() }
            }
            .doAfterSuccess { tempFile.delete() }
    }
}
