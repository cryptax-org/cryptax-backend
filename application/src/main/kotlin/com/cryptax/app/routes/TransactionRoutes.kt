package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.config.Config
import com.cryptax.controller.model.TransactionWeb
import com.cryptax.validation.RestValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import com.cryptax.validation.RestValidation.transactionBodyValidation
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleTransactionRoutes(config: Config, router: Router, jwtAuthHandler: JWTAuthHandler) {

    val transactionController = config.transactionController()

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
            val result = transactionController.addTransaction(userId, transactionWeb)
            sendSuccess(JsonObject.mapFrom(result), routingContext.response())
        }
        .failureHandler(failureHandler)

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
            val result = transactionController.addTransaction(userId, transactionWeb)
            sendSuccess(JsonObject.mapFrom(result), routingContext.response())
        }
        .failureHandler(failureHandler)

    // Get all transactions for a user with JWT token
    router.get("/users/:userId/transactions")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(bodyHandler)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val result = transactionController.getAllTransactions(userId = userId)
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
        }
        .failureHandler(failureHandler)

    // Get transaction with JWT token
    router.get("/users/:userId/transactions/:transactionId")
        .handler(jsonContentTypeValidation)
        .handler(jwtAuthHandler)
        .handler(bodyHandler)
        .handler(RestValidation.getTransactionValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            val transactionId = routingContext.request().getParam("transactionId")
            val transactionWeb = transactionController.getTransaction(id = transactionId, userId = userId)
            if (transactionWeb != null) {
                val result = JsonObject.mapFrom(transactionWeb)
                sendSuccess(result, routingContext.response())
            } else {
                Routes.sendError(404, routingContext.response())
            }
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
            val result = transactionController.updateTransaction(transactionId, userId, transactionWeb)
            sendSuccess(JsonObject.mapFrom(result), routingContext.response())
        }
        .failureHandler(failureHandler)
}
