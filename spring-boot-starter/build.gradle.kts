plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "0.10.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.2.2.RELEASE")

    api(project(":spring"))
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
            artifactId = "spring-boot-starter"
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
                name.set("GraphQL Kotlin Toolkit: Spring Boot Starter")
                description.set("Spring Boot Starter for the GraphQL Spring integration")
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
