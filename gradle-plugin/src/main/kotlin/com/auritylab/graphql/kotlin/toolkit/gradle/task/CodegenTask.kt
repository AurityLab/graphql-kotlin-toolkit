package com.auritylab.graphql.kotlin.toolkit.gradle.task

import com.auritylab.graphql.kotlin.toolkit.codegen.Codegen
import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import java.nio.file.Path
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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

    @Input
    @Optional
    val enableSpringBootIntegration = project.objects.property<Boolean>()

    @TaskAction
    fun doGenerate() {
        val codegen = Codegen(buildCodegenOptions())

        // Start the generation process.
        codegen.generate()
    }

    /**
     * Will build a new [CodegenOptions] instance with the given tasks options.
     */
    private fun buildCodegenOptions(): CodegenOptions {
        val options = CodegenOptions(
            getSchemaPaths(),
            getOutputDirectoryPath()
        )

        generatedGlobalPrefix.orNull?.also {
            options.generatedGlobalPrefix = it
        }

        generatedBasePackage.orNull?.also {
            options.generatedBasePackage = it
        }

        generateAll.orNull?.also {
            options.generateAll = it
        }

        enableSpringBootIntegration.orNull?.also {
            options.enableSpringBootIntegration = it
        }

        return options
    }

    /**
     * Will return all schema paths in a list.
     * If no schemas are set it will throw an exception.
     */
    private fun getSchemaPaths(): List<Path> {
        // Check if there are any schemas.
        if (schemas.isEmpty) throw IllegalStateException("No schemas provided")

        // Map the files to paths.
        return schemas.files.map { it.toPath() }
    }

    /**
     * Will return the output directory path.
     * If no output directory is set it will throw an exception.
     */
    private fun getOutputDirectoryPath(): Path {
        if (!outputDirectory.isPresent)
            throw IllegalStateException("No output directory provided")

        return outputDirectory.asFile.get().toPath()
    }
}
