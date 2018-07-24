package com.cryptax.app

import com.cryptax.app.verticle.EmailVerticle
import com.cryptax.app.verticle.RestVerticle
import com.cryptax.config.AppConfig
import com.cryptax.config.DefaultAppConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main {

    init {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
    }

    private val log: Logger = LoggerFactory.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val dropwizardOptions = DropwizardMetricsOptions(baseName = "cryptax", enabled = true)
        val vertx = Vertx.vertx(VertxOptions().setMetricsOptions(dropwizardOptions))
        val appConfig = DefaultAppConfig()
        vertx.deployVerticle(RestVerticle(appConfig))
        vertx.deployVerticle(EmailVerticle(appConfig)) { ar: AsyncResult<String> ->
            when {
                ar.succeeded() -> log.info("${EmailVerticle::class.java.simpleName} deployed")
                ar.failed() -> log.error("Could not deploy ${EmailVerticle::class.java.simpleName}", ar.cause())
            }
        }
    }
}
