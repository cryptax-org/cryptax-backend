plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.health"

dependencies {
    implementation(project(":domain"))
    implementation(Lib.dropwizard_healthchecks)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
