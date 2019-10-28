plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.commons_codec)
}
