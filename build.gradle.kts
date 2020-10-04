version = "0.6.3"
group = "com.auritylab.graphql-kotlin-toolkit"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.10" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    id("org.jetbrains.dokka") version "0.10.1" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    group = parent!!.group
    version = parent!!.version

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    dependencies {
        "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.0")
        "testImplementation"("org.junit.jupiter:junit-jupiter-engine:5.7.0")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:5.7.0")
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

    tasks.create("sourceJar", Jar::class) {
        archiveClassifier.set("sources")
        afterEvaluate {
            from(the<SourceSetContainer>().getByName("main").allSource)
        }
    }

    tasks.create("javadocJar", Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(tasks.getByName("dokka"))
    }

    afterEvaluate {
        // Check if publication shall be configured for this project.
        val publicationEnabled = ext.properties["publication.enabled"]?.let { it as? Boolean } ?: false
        val publicationArtifactId = ext.properties["publication.artifactId"]?.let { it as? String } ?: ""
        val publicationName = ext.properties["publication.name"]?.let { it as? String } ?: ""
        val publicationDescription = ext.properties["publication.description"]?.let { it as? String } ?: ""

        if (publicationEnabled) {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        artifactId = publicationArtifactId
                        from(components.getByName("java"))
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
                            name.set(publicationName)
                            description.set(publicationDescription)
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
        }

        configure<SigningExtension> {
            the<PublishingExtension>().publications
                .findByName("maven")
                ?.let { sign(it) }
        }
    }
}

tasks.create("allPublish") {
    group = "publishing"

    dependsOn(":graphql-kotlin-toolkit-codegen:publish")
    dependsOn(":graphql-kotlin-toolkit-common:publish")
    dependsOn(":graphql-kotlin-toolkit-spring-boot:publish")
    dependsOn(":graphql-kotlin-toolkit-jpa:publish")
    dependsOn(":graphql-kotlin-toolkit-gradle-plugin:publishPlugins")
}
