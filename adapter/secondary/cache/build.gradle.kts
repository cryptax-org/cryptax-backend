plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.cache"

dependencies {
    implementation(project(":domain"))
    implementation(Lib.rx)
    implementation(Lib.hazelcast)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
