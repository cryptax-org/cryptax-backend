package com.cryptax.app.route

import com.cryptax.app.exception.ParamException
import com.cryptax.controller.TransactionController
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.Source
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.adapter.rxjava.toMono
import java.io.InputStream
import java.io.SequenceInputStream
import java.time.Duration

@RestController
class TransactionRoutes @Autowired constructor(private val transactionController: TransactionController) {

    @PostMapping("/users/{userId}/transactions")
    fun addTransaction(
        @RequestBody transactionWeb: TransactionWeb,
        @PathVariable userId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.addTransaction(userId, transactionWeb).toMono() }
    }

    @PostMapping("/users/{userId}/transactions/bulk")
    fun addMultipleTransactions(
        @RequestBody transactions: List<TransactionWeb>,
        @PathVariable userId: String): Mono<List<TransactionWeb>> {
        return verifyUserId(userId).flatMap { transactionController.addMultipleTransactions(userId, transactions).toMono() }
    }

    @GetMapping("/users/{userId}/transactions")
    fun getAllTransactions(@PathVariable userId: String): Mono<List<TransactionWeb>> {
        return verifyUserId(userId).flatMap { transactionController.getAllTransactions(userId).toMono() }
    }

    @GetMapping("/users/{userId}/transactions/{transactionId}")
    fun getOneTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.getTransaction(transactionId, userId).toMono() }
    }

    @PutMapping("/users/{userId}/transactions/{transactionId}")
    fun updateTransaction(
        @RequestBody transactionWeb: TransactionWeb,
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<TransactionWeb> {
        return verifyUserId(userId).flatMap { transactionController.updateTransaction(transactionId, userId, transactionWeb).toMono() }
    }

    @DeleteMapping("/users/{userId}/transactions/{transactionId}")
    fun deleteTransaction(
        @PathVariable userId: String,
        @PathVariable transactionId: String): Mono<Unit> {
        return verifyUserId(userId).flatMap { transactionController.deleteTransaction(transactionId, userId).toMono() }
    }

    @PostMapping("/users/{userId}/transactions/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
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
    }
}
