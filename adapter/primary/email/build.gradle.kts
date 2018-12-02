plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.email"

dependencies {
    implementation(project(":domain"))
    implementation(project(":config"))
    implementation(Lib.okhttp)
    implementation(Lib.jackson_databind)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
    testImplementation(Lib.mockito_kotlin)
}
