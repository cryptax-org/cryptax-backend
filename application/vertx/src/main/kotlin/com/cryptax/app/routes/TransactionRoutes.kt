package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.controller.TransactionController
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.domain.entity.Source
import com.cryptax.validation.RestValidation.csvContentTypeValidation
import com.cryptax.validation.RestValidation.deleteTransactionValidation
import com.cryptax.validation.RestValidation.getTransactionValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.transactionBodyValidation
import com.cryptax.validation.RestValidation.uploadCsvValidation
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import java.io.ByteArrayInputStream

fun handleTransactionRoutes(router: Router, jwtAuthHandler: JWTAuthHandler, vertxScheduler: Scheduler, transactionController: TransactionController) {

    // Add transaction to user with JWT token
    router.post("/users/:userId/transactions")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(bodyHandler)
        .handler(transactionBodyValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val body = routingContext.body
            val transactionWeb = body.toJsonObject().mapTo(TransactionWeb::class.java)
            transactionController
                .addTransaction(userId, transactionWeb)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { result -> sendSuccess(JsonObject.mapFrom(result), routingContext.response()) },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Get all transactions for a user with JWT token
    router.get("/users/:userId/transactions")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(bodyHandler)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            transactionController
                .getAllTransactions(userId = userId)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { transactionsWeb ->
                        val result = transactionsWeb
                            .map { JsonObject.mapFrom(it) }
                            .fold(mutableListOf<JsonObject>()) { accumulator, item ->
                                accumulator.add(item)
                                accumulator
                            }
                            .fold(JsonArray()) { accumulator, item ->
                                accumulator.add(item)
                                accumulator
                            }
                        sendSuccess(result, routingContext.response())
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Get transaction with JWT token
    router.get("/users/:userId/transactions/:transactionId")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(getTransactionValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val transactionId = routingContext.request().getParam("transactionId")
            transactionController
                .getTransaction(id = transactionId, userId = userId)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { transactionWeb ->
                        val result = JsonObject.mapFrom(transactionWeb)
                        sendSuccess(result, routingContext.response())
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Update transaction with JWT token
    router.put("/users/:userId/transactions/:transactionId")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(bodyHandler)
        .handler(transactionBodyValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val transactionId = routingContext.request().getParam("transactionId")
            val transactionWeb = routingContext.body.toJsonObject().mapTo(TransactionWeb::class.java)
            transactionController.updateTransaction(transactionId, userId, transactionWeb)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { transactionsWeb -> sendSuccess(JsonObject.mapFrom(transactionsWeb), routingContext.response()) },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Update transaction with JWT token
    router.delete("/users/:userId/transactions/:transactionId")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(deleteTransactionValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val transactionId = routingContext.request().getParam("transactionId")
            transactionController.deleteTransaction(transactionId, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { _ -> routingContext.response().end() },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)

    // Upload CSV
    router.post("/users/:userId/transactions/upload")
        .handler(csvContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(uploadCsvValidation)
        .handler(bodyHandler)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val source = routingContext.request().getParam("source")
            val delimiter = routingContext.request().getParam("delimiter")
            val body = routingContext.body

            transactionController
                .uploadCSVTransactions(
                    inputStream = ByteArrayInputStream(body.bytes),
                    userId = userId,
                    source = Source.valueOf(source.toUpperCase()),
                    delimiter = if (delimiter == null) ',' else delimiter.toCharArray()[0])
                .subscribeOn(Schedulers.io())
                .observeOn(vertxScheduler)
                .subscribe(
                    { transactionsWeb ->
                        val result = transactionsWeb
                            .map { JsonObject.mapFrom(it) }
                            .fold(mutableListOf<JsonObject>()) { accumulator, item ->
                                accumulator.add(item)
                                accumulator
                            }
                            .fold(JsonArray()) { accumulator, item ->
                                accumulator.add(item)
                                accumulator
                            }
                        sendSuccess(result, routingContext.response())
                    },
                    { error -> routingContext.fail(error) })
        }
        .failureHandler(failureHandler)
}
