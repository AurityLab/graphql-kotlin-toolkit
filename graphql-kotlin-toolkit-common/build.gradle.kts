ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "common"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Common"
    this["publication.description"] = "GraphQL Code generator for Kotlin"
}

dependencies {
    implementation("com.graphql-java:graphql-java:15.0")
}
