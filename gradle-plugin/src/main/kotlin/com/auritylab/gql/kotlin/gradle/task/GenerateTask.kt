package com.auritylab.gql.kotlin.gradle.task

import com.auritylab.graphql.kotlin.codegen.Codegen
import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class GenerateTask : DefaultTask() {
    @InputFiles
    val schemas = project.objects.fileCollection()

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @TaskAction
    fun doGenerate() {
        val inputSchemaFiles = schemas.files.map { it.toPath() }
        val outputDirectoryPath = outputDirectory.asFile.get().toPath()

        println(inputSchemaFiles)
        println(outputDirectoryPath)

        val options = CodegenOptions(outputDirectory = outputDirectoryPath)

        Codegen(options, inputSchemaFiles)
    }
}
