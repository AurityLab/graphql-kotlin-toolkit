plugins {
    id("org.jetbrains.kotlin.kapt")
}

ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "util"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Util"
    this["publication.description"] = "Util"
}

dependencies {
    implementation(project(":graphql-kotlin-toolkit-common"))
    implementation(project(":graphql-kotlin-toolkit-codegen-binding"))

    // JPA API.
    compileOnly("jakarta.persistence:jakarta.persistence-api:2.2.3")

    // GraphQL-Java dependency.
    implementation("com.graphql-java:graphql-java:15.0")

    // Test dependencies.
    testImplementation("me.lazmaid.kraph:kraph:0.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}
