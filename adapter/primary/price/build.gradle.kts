plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":cache"))
    implementation(Lib.okhttp)
    implementation(Lib.jackson_databind)
    implementation(Lib.rx)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
    testImplementation(Lib.mockito_kotlin)
}
