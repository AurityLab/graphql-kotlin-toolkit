plugins {
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.spring")
}

ext {
    this["publication.enabled"] = true
    this["publication.artifactId"] = "spring-boot"
    this["publication.name"] = "GraphQL Kotlin Toolkit: Spring"
    this["publication.description"] = "GraphQL integration for Spring"
    this["jacoco.merge.enabled"] = true
}

dependencies {
    implementation(project(":graphql-kotlin-toolkit-common"))

    // Spring (Boot) dependencies.
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.2.2.RELEASE")
    implementation("org.springframework:spring-webmvc:5.2.2.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
    kapt("org.springframework.boot:spring-boot-configuration-processor:2.2.2.RELEASE")

    // GraphQL-Java dependency.
    implementation("com.graphql-java:graphql-java:16.2")

    implementation("com.auritylab:kotlin-object-path:1.0.0")

    // Test dependencies.
    testImplementation("org.springframework.boot:spring-boot-starter-web:2.2.2.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.2.2.RELEASE") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("com.github.VerachadW:kraph:v.0.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}
