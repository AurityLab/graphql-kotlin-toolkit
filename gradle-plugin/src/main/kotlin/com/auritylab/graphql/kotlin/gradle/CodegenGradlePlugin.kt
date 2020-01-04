package com.auritylab.graphql.kotlin.gradle

import com.auritylab.graphql.kotlin.gradle.extension.CodegenExtension
import com.auritylab.graphql.kotlin.gradle.task.CodegenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class CodegenGradlePlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_GROUP = "GraphQL Kotlin Codegen"
    }

    override fun apply(project: Project) {
        val generateExtension = project.extensions.create<CodegenExtension>("graphqlKotlinCodegen")
        val defaultOutputDirectory = project.layout.buildDirectory.dir("generated/graphql/kotlin/main/")

        generateExtension.outputDirectory.set(defaultOutputDirectory)



        project.afterEvaluate {
            val kotlinProjectExtension = this.extensions.findByType<KotlinProjectExtension>()
                    ?: throw IllegalStateException("Plugin 'org.jetbrains.kotlin.jvm' not applied.")

            kotlinProjectExtension.sourceSets {
                "main" {
                    kotlin.srcDir(defaultOutputDirectory)
                }
            }

            tasks.create("graphqlKotlinCodegen", CodegenTask::class) {
                group = PLUGIN_GROUP

                schemas.setFrom(generateExtension.schemas)
                outputDirectory.set(generateExtension.outputDirectory)
                generatedGlobalPrefix.set(generateExtension.generatedGlobalPrefix)
                generatedBasePackage.set(generateExtension.generatedBasePackage)
            }

            tasks.withType<KotlinCompile>() {
                this.dependsOn("graphqlKotlinCodegen")
            }
        }
    }
}
