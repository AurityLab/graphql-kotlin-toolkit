plugins {
    id("org.jetbrains.kotlin.kapt")
    id("signing")
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    // Kotlin dependencies.
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":graphql-kotlin-toolkit-common"))

    // Spring (Boot) dependencies.
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.2.2.RELEASE")
    implementation("org.springframework:spring-webmvc:5.2.2.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
    kapt("org.springframework.boot:spring-boot-configuration-processor:2.2.2.RELEASE")

    // GraphQL-Java dependency.
    implementation("com.graphql-java:graphql-java:13.0")

    implementation("com.auritylab:kotlin-object-path:1.0.0")

    // Test dependencies.
    testImplementation("org.springframework.boot:spring-boot-starter-web:2.2.2.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.2.2.RELEASE") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testImplementation("me.lazmaid.kraph:kraph:0.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks.create("sourceJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.create("javadocJar", Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "spring-boot"
            from(components["java"])
            artifact(tasks.getByName("sourceJar"))
            artifact(tasks.getByName("javadocJar"))

            repositories {
                maven {
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                    credentials {
                        username = if (project.hasProperty("ossrhUsername"))
                            project.property("ossrhUsername") as String else null
                        password = if (project.hasProperty("ossrhPassword"))
                            project.property("ossrhPassword") as String else null
                    }
                }
            }

            pom {
                name.set("GraphQL Kotlin Toolkit: Spring")
                description.set("GraphQL integration for Spring")
                url.set("https://github.com/AurityLab/graphql-kotlin-toolkit")

                organization {
                    name.set("AurityLab UG (haftungsbeschraenkt)")
                    url.set("https://github.com/AurityLab")
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/AurityLab/graphql-kotlin-toolkit/issues")
                }

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/AurityLab/graphql-kotlin-toolkit/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                scm {
                    url.set("https://github.com/AurityLab/graphql-kotlin-toolkit")
                    connection.set("scm:git:git://github.com/AurityLab/graphql-kotlin-toolkit.git")
                    developerConnection.set("scm:git:ssh://git@github.com:AurityLab/graphql-kotlin-toolkit.git")
                }

                developers {
                    developer {
                        id.set("WipeAir")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications.getByName("maven"))
}
