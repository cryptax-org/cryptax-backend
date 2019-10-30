buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone")
    }
    dependencies {
        classpath(Lib.spring_boot_gradle)
        classpath(Lib.kotlin_gradle)
        classpath(Lib.kotlin_allopen)
        classpath(Lib.nebula)
    }
}

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.10"
    id("org.springframework.boot") version Version.spring_boot
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("nebula.info") version "4.0.3"
}

repositories {
    mavenCentral()
    maven(url = "https://repo.spring.io/milestone")
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "org.springframework", module = "spring-jcl")
    exclude(group = "com.google.code.findbugs", module = "jsr305")
    exclude(group = "org.mockito", module = "mockito-core")
    exclude(group = "junit", module = "junit")
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":cloud-datastore"))
    implementation(project(":config"))
    implementation(project(":controller"))
    implementation(project(":domain"))
    implementation(project(":email"))
    implementation(project(":health"))
    implementation(project(":in-memory-db-simple"))
    implementation(project(":id-generator"))
    implementation(project(":jwt"))
    implementation(project(":price"))
    implementation(project(":security"))
    implementation(project(":usecase"))

    implementation(Lib.spring_boot_webflux)
    implementation(Lib.spring_boot_log4j2)
    implementation(Lib.spring_boot_security)
    implementation(Lib.jackson_annotations)
    implementation(Lib.jackson_yaml)
    implementation(Lib.jackson_kotlin)
    implementation(Lib.hazelcast)
    implementation(Lib.hazelcast_spring)
    implementation(Lib.okhttp)
    implementation(Lib.google_cloud_datasource)
    implementation(Lib.google_auth)
    implementation(Lib.hibernate_validator)
    implementation(Lib.jaxb)
    implementation(Lib.rx)
    implementation(Lib.reactor)
    implementation(Lib.reactor_addons)
    implementation(Lib.dropwizard_healthchecks)

    testImplementation(Lib.spring_boot_test)
    testImplementation(Lib.rest_assured)
}

val copyJarToRoot: Task by tasks.creating {
    doLast {
        val jarName = "application-$version.jar"
        val outputDirectory = "$rootDir/build"

        val currentJar = file("$buildDir/libs/$jarName")
        val outputJar = file("$outputDirectory/${rootProject.name}"+".jar")

        currentJar.renameTo(outputJar)
        println("Move $jarName to $outputDirectory/${rootProject.name}" + ".jar")
    }
}

tasks.getByName("build").finalizedBy(copyJarToRoot)
