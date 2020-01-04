plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-gradle-plugin")
    id("org.gradle.kotlin.kotlin-dsl") version "1.3.1"
    id("maven-publish")
}

repositories {
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(gradleApi())
    implementation(project(":codegen"))
}

gradlePlugin {
    plugins {
        create("graphql-kotlin-toolkit-codegen") {
            id = "org.auritylab.graphql.kotlin.toolkit.codegen"
            implementationClass = "com.auritylab.gql.kotlin.gradle.PoetGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.auritylab.graphql.kotlin.toolkit"
            artifactId = "gradle-plugin"
            version = "1.0.0"

            from(components["java"])
        }
    }
}
