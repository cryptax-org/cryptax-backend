package com.cryptax.controller

import com.cryptax.controller.model.TransactionWeb
import com.cryptax.controller.utils.sendError
import com.cryptax.controller.utils.sendSuccess
import com.cryptax.usecase.transaction.AddTransaction
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class TransactionController(private val addTransaction: AddTransaction) {

	fun addTransaction(routingContext: RoutingContext) {
		val response = routingContext.response()
		val userId = routingContext.request().getParam("userId")
		val body = routingContext.body

		if (routingContext.user().principal().getString("id") == userId) {
			val transactionWeb = body.toJsonObject().mapTo(TransactionWeb::class.java)
			val result = addTransaction.add(transactionWeb.toTransaction(userId))
			sendSuccess(JsonObject.mapFrom(TransactionWeb.toTransactionWeb(result)), response)
		} else {
			sendError(401, response)
		}
	}
}
