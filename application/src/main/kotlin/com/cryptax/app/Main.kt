package com.cryptax.app

import com.cryptax.app.verticle.EmailVerticle
import com.cryptax.app.verticle.RestVerticle
import com.cryptax.config.DefaultAppConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import io.vertx.reactivex.ext.mail.MailClient
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
            when {
                ar.succeeded() -> {
                    val vertx: Vertx = ar.result()
                    val appConfig = DefaultAppConfig()
                    vertx.deployVerticle(RestVerticle(appConfig)) { arRest: AsyncResult<String> ->
                        when {
                            arRest.succeeded() -> log.info("${RestVerticle::class.java.simpleName} deployed")
                            arRest.failed() -> log.error("Could not deploy ${EmailVerticle::class.java.simpleName}", arRest.cause())
                        }
                    }
                    val mailClient = MailClient.createShared(io.vertx.reactivex.core.Vertx(vertx), appConfig.mailConfig, "CRYPTAX_POOL")
                    vertx.deployVerticle(EmailVerticle(appConfig, mailClient)) { arEmail: AsyncResult<String> ->
                        when {
                            arEmail.succeeded() -> log.info("${EmailVerticle::class.java.simpleName} deployed")
                            arEmail.failed() -> log.error("Could not deploy ${EmailVerticle::class.java.simpleName}", arEmail.cause())
                        }
                    }
                }
                else -> log.error("Could not deploy start clustered vertx", ar.cause())
            }
        }
    }
}
