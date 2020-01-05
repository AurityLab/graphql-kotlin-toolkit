plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50" apply false
    id("org.gradle.kotlin.kotlin-dsl") version "1.3.1" apply false
}

subprojects {
    group = "com.auritylab.graphql-kotlin-toolkit"
    version = "0.1.0"

    repositories {
        jcenter()
    }
}
