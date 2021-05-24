ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "codegen"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Codegen"
    this["publication.description"] = "GraphQL Code generator for Kotlin"
    this["jacoco.merge.enabled"] = true
}

dependencies {
    implementation("com.graphql-java:graphql-java:16.2")
    implementation("com.squareup:kotlinpoet:1.7.2")

    implementation(project(":graphql-kotlin-toolkit-common"))

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(project(":graphql-kotlin-toolkit-codegen-binding"))
}

