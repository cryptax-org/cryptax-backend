object Version {
    // Implementation
    val jackson = "2.9.7"
    val rx = "2.2.3"

    // Test
    val mockito = "2.23.0"
    val mockito_kotlin = "2.0.0"
}

object Lib {
    // Implementation
    val jackson_databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    val jackson_kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    val jackson_jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}"
    val rx = "io.reactivex.rxjava2:rxjava:${Version.rx}"

    // Test
    val mockito = "org.mockito:mockito-core:${Version.mockito}"
    val mockito_junit = "org.mockito:mockito-junit-jupiter:${Version.mockito}"
    val mockito_kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Version.mockito_kotlin}"
}
