plugins {
    `java-library`
}

dependencies {
    implementation(project(":domain"))
    implementation(Lib.opencsv)
    implementation(Lib.commons_lang3)
}
