plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.rx)

    testImplementation(Lib.jackson_databind)
    testImplementation(Lib.jackson_kotlin)
    testImplementation(Lib.jackson_jsr310)
}
