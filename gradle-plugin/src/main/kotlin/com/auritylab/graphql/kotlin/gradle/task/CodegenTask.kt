package com.auritylab.graphql.kotlin.gradle.task

import com.auritylab.graphql.kotlin.codegen.Codegen
import com.auritylab.graphql.kotlin.codegen.CodegenOptions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.property

open class CodegenTask : DefaultTask() {
    @InputFiles
    val schemas = project.objects.fileCollection()

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @Input
    @Optional
    val generatedGlobalPrefix = project.objects.property<String>()

    @Input
    @Optional
    val generatedBasePackage = project.objects.property<String>()

    @Input
    @Optional
    val generateAll = project.objects.property<Boolean>()

    @TaskAction
    fun doGenerate() {
        val schemas =
                if (schemas.isEmpty) throw IllegalStateException("No schemas set")
                else schemas.files.map { it.toPath() }

        val outputDirectory = outputDirectory.orNull?.asFile?.toPath()
                ?: throw IllegalStateException("No output directory set")

        val options = CodegenOptions(
                schemas,
                outputDirectory,
                generatedGlobalPrefix.orNull,
                generatedBasePackage.orNull,
                generateAll.orNull)

        val codegen = Codegen(options)

        // Start the generation process.
        codegen.generate()
    }
}
