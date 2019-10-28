plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.rx)
    implementation(Lib.hazelcast)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
