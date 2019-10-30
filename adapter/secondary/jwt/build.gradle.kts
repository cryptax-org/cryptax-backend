plugins {
    `java-library`
}

dependencies {
    implementation(project(":config"))

    implementation(Lib.jjwt)
    implementation(Lib.rx)

    testImplementation(Lib.mockito)
    testImplementation(Lib.mockito_junit)
}
