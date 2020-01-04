package com.auritylab.graphql.kotlin.gradle.task

import com.auritylab.graphql.kotlin.codegen.Codegen
import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class CodegenTask : DefaultTask() {
    @InputFiles
    val schemas = project.objects.fileCollection()

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @TaskAction
    fun doGenerate() {
        val inputSchemaFiles = schemas.files.map { it.toPath() }
        val outputDirectoryPath = outputDirectory.asFile.get().toPath()

        val codegen = Codegen(CodegenOptions(outputDirectory = outputDirectoryPath), inputSchemaFiles)

        // Start the generation process.
        codegen.generate()
    }
}
