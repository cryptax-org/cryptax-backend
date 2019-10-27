plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.controller"

configurations.all {
    resolutionStrategy {
        force("com.fasterxml:classmate:1.4.0")
    }
}

dependencies {
    implementation(project(":usecase"))
    implementation(project(":domain"))
    implementation(project(":parser"))
    implementation(Lib.rx)
    implementation(Lib.hibernate_validator)
    implementation("io.micronaut:micronaut-core:1.2.5")

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
    testImplementation(Lib.mockito_kotlin)
    testImplementation(Lib.byte_buddy)
}
