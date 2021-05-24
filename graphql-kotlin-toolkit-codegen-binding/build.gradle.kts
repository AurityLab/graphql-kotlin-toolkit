plugins {
    id("org.jetbrains.kotlin.kapt")
}

ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "codegen-binding"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Codegen binding"
    this["publication.description"] = "Binding for Codegen"
}

dependencies {
    implementation(project(":graphql-kotlin-toolkit-common"))

    // GraphQL-Java dependency.
    implementation("com.graphql-java:graphql-java:16.2")

    // Test dependencies.
    testImplementation("com.github.VerachadW:kraph:v.0.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}
