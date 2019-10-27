package com.cryptax.app

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("com.cryptax.app")
            .mainClass(Application.javaClass)
            .start()
    }
}


