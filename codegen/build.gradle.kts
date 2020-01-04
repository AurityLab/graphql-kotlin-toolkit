

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

repositories {
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.graphql-java:graphql-java:13.0")
    implementation("com.squareup:kotlinpoet:1.4.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.auritylab.graphql.kotlin.toolkit"
            artifactId = "codegen"
            version = "1.0.0"

            from(components["java"])
        }
    }
}
