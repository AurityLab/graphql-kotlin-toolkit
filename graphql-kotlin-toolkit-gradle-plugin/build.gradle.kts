plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.gradle.kotlin.kotlin-dsl") version "1.3.5"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.71")
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
