package com.auritylab.gql.kotlin.gradle

import com.auritylab.gql.kotlin.gradle.extension.GenerateExtension
import com.auritylab.gql.kotlin.gradle.task.GenerateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class PoetGradlePlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_GROUP = "GraphQL Kotlin Toolkit"
    }

    override fun apply(project: Project) {
        project.run {
            val generateExtension = extensions.create<GenerateExtension>("graphqlKotlinGenerate")
            val defaultOutputDirectory = project.layout.buildDirectory.dir("generated/graphql/kotlin/main/")

            generateExtension.outputDirectory.set(defaultOutputDirectory)

            /*the<JavaPluginConvention>().sourceSets {
                "main" {
                    java {
                        srcDirs(defaultOutputDirectory)
                    }
                }
            }*/

            this.afterEvaluate {
                the<KotlinJvmProjectExtension>().sourceSets {
                    "main" {
                        kotlin.srcDir(defaultOutputDirectory)
                    }
                }
            }

            tasks.apply {
                create("graphqlKotlinGenerate", GenerateTask::class) {
                    group = PLUGIN_GROUP

                    schemas.setFrom(generateExtension.schemas)
                    outputDirectory.set(generateExtension.outputDirectory)
                }

            }
        }
    }

}
