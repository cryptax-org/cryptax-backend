plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.dropwizard_healthchecks)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
