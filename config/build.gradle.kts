plugins {
    `java-library`
}

extra["moduleName"] = "cryptax.config"

dependencies {
    implementation(Lib.jasypt)
    implementation(Lib.jackson_annotations)
    implementation(Lib.jackson_kotlin)
    implementation(Lib.jackson_yaml)
}
