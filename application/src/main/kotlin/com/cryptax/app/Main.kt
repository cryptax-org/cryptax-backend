package com.cryptax.app

import com.cryptax.app.verticle.EmailVerticle
import com.cryptax.app.verticle.RestVerticle
import com.cryptax.config.DefaultAppConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main {

    init {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        System.setProperty("hazelcast.logging.type", "slf4j")
    }

    private val log: Logger = LoggerFactory.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val dropwizardOptions = DropwizardMetricsOptions(baseName = "cryptax", enabled = true)

        val mgr = HazelcastClusterManager()
        Vertx.clusteredVertx(VertxOptions().setClusterManager(mgr).setMetricsOptions(dropwizardOptions)) { ar ->
            if (ar.succeeded()) {
                val vertx = ar.result()
                val appConfig = DefaultAppConfig()
                vertx.deployVerticle(RestVerticle(appConfig))
                vertx.deployVerticle(EmailVerticle(appConfig)) { ar: AsyncResult<String> ->
                    when {
                        ar.succeeded() -> log.info("${EmailVerticle::class.java.simpleName} deployed")
                        ar.failed() -> log.error("Could not deploy ${EmailVerticle::class.java.simpleName}", ar.cause())
                    }
                }
            } else {
                log.error("Could not deploy start clustered vertx", ar.cause())
            }
        }
    }
}
