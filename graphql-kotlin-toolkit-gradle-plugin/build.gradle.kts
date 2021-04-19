plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.gradle.kotlin.kotlin-dsl") version "2.1.4"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
    implementation(gradleApi())
    implementation(project(":graphql-kotlin-toolkit-codegen"))
}

gradlePlugin {
    plugins {
        create("graphql-kotlin-toolkit-codegen") {
            id = "com.auritylab.graphql-kotlin-toolkit.codegen"
            displayName = "GraphQL Kotlin Toolkit: Codegen"
            description = "GraphQL code generator for Kotlin"
            implementationClass = "com.auritylab.graphql.kotlin.toolkit.gradle.CodegenGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/AurityLab/graphql-kotlin-toolkit"
    vcsUrl = "https://github.com/AurityLab/graphql-kotlin-toolkit"
    tags = listOf("graphql", "kotlin", "codegen", "codegeneration", "graphql-codegen")
}
