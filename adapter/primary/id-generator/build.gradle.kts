plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.java_uuid)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
