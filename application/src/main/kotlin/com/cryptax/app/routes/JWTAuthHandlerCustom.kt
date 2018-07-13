package com.cryptax.app.routes

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.handler.impl.HttpStatusException
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl

class JWTAuthHandlerCustom(authProvider: JWTAuth) : JWTAuthHandlerImpl(authProvider, null) {

    override fun authorize(user: User, handler: Handler<AsyncResult<Void>>) {
        val isRefresh = user.principal().getBoolean("isRefresh")
        if (isRefresh) {
            throw HttpStatusException(403)
        }
        super.authorize(user, handler)
    }
}

class JWTRefreshAuthHandlerCustom(authProvider: JWTAuth) : JWTAuthHandlerImpl(authProvider, null) {

    override fun authorize(user: User, handler: Handler<AsyncResult<Void>>) {
        val isRefresh = user.principal().getBoolean("isRefresh")
        if (isRefresh) {
            super.authorize(user, handler)
            return
        }
        throw HttpStatusException(403)
    }
}

