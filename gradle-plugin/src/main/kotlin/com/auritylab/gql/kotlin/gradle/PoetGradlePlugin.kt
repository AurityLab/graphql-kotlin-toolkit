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
            val generateExtension = extensions.create<GenerateExtension>("graphqlKotlinGenerate")

            generateExtension.outputDirectory.set(project.layout.buildDirectory.dir("generate-resources/main"))

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
