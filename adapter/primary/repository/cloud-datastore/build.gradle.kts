plugins {
    `java-library`
}

configurations.all {
    exclude(module = "grpc-context")
    resolutionStrategy {
        force("com.google.code.gson:gson:2.8.5")
        force("joda-time:joda-time:2.10")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.rx)
    implementation(Lib.google_cloud_datasource)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
    testImplementation(Lib.mockito_kotlin)
}
