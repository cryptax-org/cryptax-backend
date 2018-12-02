plugins {
    `java-library`
}


extra["moduleName"] = "cryptax.parser"

dependencies {
    implementation(project(":domain"))
    implementation(Lib.opencsv)
    implementation(Lib.commons_lang3)
}
