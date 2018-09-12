package com.cryptax.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val springApplication = SpringApplication(Application::class.java)
    springApplication.webApplicationType = WebApplicationType.REACTIVE
    runApplication<Application>(*args)
}
