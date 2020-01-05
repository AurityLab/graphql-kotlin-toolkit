plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-gradle-plugin")
    id("org.gradle.kotlin.kotlin-dsl")
    id("maven-publish")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
    implementation(gradleApi())
    implementation(project(":codegen"))
}

gradlePlugin {
    plugins {
        create("graphql-kotlin-toolkit-codegen") {
            id = "org.auritylab.graphql.kotlin.codegen"
            implementationClass = "com.auritylab.graphql.kotlin.gradle.CodegenGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.auritylab.graphql.kotlin.codegen"
            artifactId = "gradle-plugin"
            version = "1.0.0"

            from(components["java"])
        }
    }
}
