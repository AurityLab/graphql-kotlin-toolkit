plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "0.10.0"
}

dependencies {
    "implementation"(project(":spring"))
}
