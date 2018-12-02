// @formatter:off
object Version {
    // Implementation
    val byte_buddy              = "1.9.4"
    val commons_codec           = "1.11"
    val commons_lang3           = "3.8.1"
    val dropwizard_metrics      = "4.0.3"
    val google_cloud_datasource = "1.53.0"
    val hazelcast               = "3.11"
    val hibernate_validator     = "6.0.13.Final"
    val jackson                 = "2.9.7"
    val jasypt                  = "1.9.2"
    val java_uuid               = "3.1.5"
    val okhttp                  = "3.11.0"
    val opencsv                 = "4.3.2"
    val rx                      = "2.2.3"

    // Test
    val mockito                 = "2.23.0"
    val mockito_kotlin          = "2.0.0"
}

object Lib {
    // Implementation
    val commons_codec           = "commons-codec:commons-codec:${Version.commons_codec}"
    val commons_lang3           = "org.apache.commons:commons-lang3:${Version.commons_lang3}"
    val dropwizard_healthchecks = "io.dropwizard.metrics:metrics-healthchecks:${Version.dropwizard_metrics}"
    val google_cloud_datasource = "com.google.cloud:google-cloud-datastore:${Version.google_cloud_datasource}"
    val hazelcast               = "com.hazelcast:hazelcast:${Version.hazelcast}"
    val hibernate_validator     = "org.hibernate.validator:hibernate-validator:${Version.hibernate_validator}"
    val jackson_annotations     = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    val jackson_databind        = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    val jackson_kotlin          = "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    val jackson_jsr310          = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}"
    val jackson_yaml            = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Version.jackson}"
    val jasypt                  = "org.jasypt:jasypt:${Version.jasypt}"
    val java_uuid               = "com.fasterxml.uuid:java-uuid-generator:${Version.java_uuid}"
    val okhttp                  = "com.squareup.okhttp3:okhttp:${Version.okhttp}"
    val opencsv                 = "com.opencsv:opencsv:${Version.opencsv}"
    val rx                      = "io.reactivex.rxjava2:rxjava:${Version.rx}"

    // Test
    val byte_buddy              = "net.bytebuddy:byte-buddy:${Version.byte_buddy}"
    val mockito                 = "org.mockito:mockito-core:${Version.mockito}"
    val mockito_junit           = "org.mockito:mockito-junit-jupiter:${Version.mockito}"
    val mockito_kotlin          = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Version.mockito_kotlin}"
}
// @formatter:on
