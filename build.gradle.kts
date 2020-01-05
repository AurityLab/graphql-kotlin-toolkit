plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50" apply false
    id("org.gradle.kotlin.kotlin-dsl") version "1.3.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    group = "com.auritylab.graphql-kotlin-toolkit"
    version = "0.1.0"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
