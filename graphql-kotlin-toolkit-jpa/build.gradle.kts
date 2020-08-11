plugins {
    id("org.jetbrains.kotlin.kapt")
    id("signing")
}

dependencies {
    implementation(project(":graphql-kotlin-toolkit-common"))

    // JPA API.
    api("jakarta.persistence:jakarta.persistence-api:2.2.3")

    // GraphQL-Java dependency.
    implementation("com.graphql-java:graphql-java:15.0")

    // Test dependencies.

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
            artifactId = "jpa"
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
                name.set("GraphQL Kotlin Toolkit: JPA")
                description.set("GraphQL Tools for JPA")
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
