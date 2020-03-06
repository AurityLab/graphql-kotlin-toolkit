plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.70" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    group = "com.auritylab.graphql-kotlin-toolkit"
    version = "0.2.2"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
