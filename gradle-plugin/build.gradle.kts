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
            id = "org.auritylab.graphql-kotlin-toolkit.codegen"
            implementationClass = "com.auritylab.graphql.kotlin.toolkit.gradle.CodegenGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "gradle-plugin"

            from(components["java"])
        }
    }
}
