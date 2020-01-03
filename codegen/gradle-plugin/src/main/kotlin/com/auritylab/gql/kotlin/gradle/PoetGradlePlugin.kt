package com.auritylab.gql.kotlin.gradle

import com.auritylab.gql.kotlin.gradle.extension.GenerateExtension
import com.auritylab.gql.kotlin.gradle.task.GenerateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class PoetGradlePlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_GROUP = "GraphQL Kotlin Toolkit"
    }

    override fun apply(project: Project) {
        project.run {
            val generateExtension = extensions.create<GenerateExtension>("graphqlKotlinGenerate", project)

            generateExtension.outputDir.set("${buildDir}/generate-resources/main")

            tasks.apply {
                create("graphqlKotlinGenerate", GenerateTask::class) {
                    group = PLUGIN_GROUP

                    inputSchemas.set(generateExtension.inputSchemas)
                    outputDir.set(generateExtension.outputDir)
                }

            }
        }
    }

}
