ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "codegen"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Codegen"
    this["publication.description"] = "GraphQL Code generator for Kotlin"
}

dependencies {
    implementation("com.graphql-java:graphql-java:15.0")
    implementation("com.squareup:kotlinpoet:1.7.2")

    implementation(project(":graphql-kotlin-toolkit-common"))

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}
