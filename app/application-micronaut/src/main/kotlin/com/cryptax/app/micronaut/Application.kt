package com.cryptax.app.micronaut

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("com.cryptax.app.micronaut")
            .mainClass(Application.javaClass)
            .start()
    }
}
