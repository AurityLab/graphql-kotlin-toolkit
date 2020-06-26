plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("org.jetbrains.dokka") version "0.10.0" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    group = "com.auritylab.graphql-kotlin-toolkit"
    version = "0.5.0"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    dependencies {
        "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.6.2")
        "testImplementation"("org.junit.jupiter:junit-jupiter-engine:5.6.2")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:5.6.2")
    }

    // Configure the dokka plugin for all subprojects.
    tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokka") {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    // Configure the test tasks to use the JUnit platform for all subprojects.
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

tasks.create("allPublish") {
    group = "publishing"

    dependsOn(":graphql-kotlin-toolkit-codegen:publish")
    dependsOn(":graphql-kotlin-toolkit-common:publish")
    dependsOn(":graphql-kotlin-toolkit-spring-boot:publish")
    dependsOn(":graphql-kotlin-toolkit-gradle-plugin:publishPlugins")
}
