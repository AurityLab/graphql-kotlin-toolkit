package com.auritylab.graphql.kotlin.gradle

import com.auritylab.graphql.kotlin.gradle.extension.CodegenExtension
import com.auritylab.graphql.kotlin.gradle.task.CodegenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class CodegenGradlePlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_GROUP = "GraphQL Kotlin Codegen"
    }

    override fun apply(project: Project) {
        project.run {
            val generateExtension = extensions.create<CodegenExtension>("graphqlKotlinCodegen")
            val defaultOutputDirectory = project.layout.buildDirectory.dir("generated/graphql/kotlin/main/")

            generateExtension.outputDirectory.set(defaultOutputDirectory)


            this.afterEvaluate {
                the<KotlinJvmProjectExtension>().sourceSets {
                    "main" {
                        kotlin.srcDir(defaultOutputDirectory)
                    }
                }
            }

            tasks.apply {
                create("graphqlKotlinCodegen", CodegenTask::class) {
                    group = PLUGIN_GROUP

                    schemas.setFrom(generateExtension.schemas)
                    outputDirectory.set(generateExtension.outputDirectory)
                }

            }
        }
    }

}
