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
    version = "0.3.0"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.create("allPublish") {
    group = "publishing"

    dependsOn(":graphql-kotlin-toolkit-codegen:publish")
    dependsOn(":graphql-kotlin-toolkit-common:publish")
    dependsOn(":graphql-kotlin-toolkit-spring-boot:publish")
    dependsOn(":graphql-kotlin-toolkit-gradle-plugin:publishPlugins")
}
