version = "0.7.2"
group = "com.auritylab.graphql-kotlin-toolkit"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.31" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.31" apply false
    id("org.jetbrains.dokka") version "1.4.32" apply false
    id("jacoco")
}

allprojects {
    repositories {
        mavenCentral()

        // JitPack repository (https://jitpack.io/) - Currently required for Kraph.
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    group = parent!!.group
    version = parent!!.version

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "jacoco")

    dependencies {
        "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.0")
        "testImplementation"("org.junit.jupiter:junit-jupiter-engine:5.7.0")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:5.7.0")
    }

    // Configure the test tasks to use the JUnit platform for all subprojects.
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Configure the Kotlin compile task.
    // Until Kotlin 1.5.0 the IR backend must be explicitly enabled (shall be enabled by default in 1.5.0).
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
        from(tasks.getByName("dokkaJavadoc"))
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

/**
 * Task which will publish all artifacts.
 */
tasks.create("allPublish") {
    group = "publishing"

    dependsOn(":graphql-kotlin-toolkit-codegen:publish")
    dependsOn(":graphql-kotlin-toolkit-codegen-binding:publish")
    dependsOn(":graphql-kotlin-toolkit-common:publish")
    dependsOn(":graphql-kotlin-toolkit-spring-boot:publish")
    dependsOn(":graphql-kotlin-toolkit-util:publish")

    dependsOn(":graphql-kotlin-toolkit-gradle-plugin:publishPlugins")
}

/**
 * Task which will collect all JaCoCo execution files and merge it into one execution file. This depends on
 * the **jacoco.merge.enabled** extension property on each project. Each subproject may define this property if it
 * should be included in the merge.
 */
val jacocoMergeTask = tasks.create("jacocoMerge", JacocoMerge::class) {
    group = "jacoco"

    // Additional function to avoid scope clash in the closures.
    val addExecutionData = { task: Task -> executionData(task) }

    subprojects {
        afterEvaluate {
            // Load the value of the property which defines if the JaCoCo merge is enabled.
            val enabled = ext.properties["jacoco.merge.enabled"]?.let { it as? Boolean } ?: false

            // If it's enabled, then add the Test task as execution data.
            if (enabled)
                tasks.withType<Test>().forEach {
                    addExecutionData(it)
                }
        }
    }
}

/**
 * Task which will create a JaCoCo report based on the merged execution file. This depends on the **jacocoMerge** task.
 */
tasks.create<JacocoReport>("jacocoMergeReport") {
    group = "jacoco"

    // Depend on the merge task.
    dependsOn(jacocoMergeTask)

    // Additional function to avoid scope clash in the closures.
    val addSourceSet = { input: SourceSet -> sourceSets(input) }

    // CodeCov depends on the XML report, therefore we need to explicitly enabled it.
    reports {
        xml.isEnabled = true
    }

    // Add the merged JaCoCo execution file as execution data for this report.
    executionData(project.file("${project.buildDir}/jacoco/jacocoMerge.exec"))

    subprojects {
        afterEvaluate {
            // Load the value of the property which defines if the JaCoCo merge is enabled.
            val enabled = ext.properties["jacoco.merge.enabled"]?.let { it as? Boolean } ?: false

            if (enabled) {
                // Load the SourceSets of the project and add it to the report task.
                val sourceSetContainer = extensions.getByName("sourceSets") as SourceSetContainer
                sourceSetContainer.forEach { containerSourceSet ->
                    if (containerSourceSet.name == "main")
                        addSourceSet(containerSourceSet)
                }
            }
        }
    }
}
