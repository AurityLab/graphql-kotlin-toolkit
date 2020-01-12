plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "0.10.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.2.2.RELEASE")

    implementation(project(":spring"))
}
