plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.security"

dependencies {
    implementation(project(":domain"))
    implementation(Lib.commons_codec)
}
