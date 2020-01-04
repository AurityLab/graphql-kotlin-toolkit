package com.auritylab.graphql.kotlin.codegen

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

internal class CodegenOutput(
        private val options: CodegenOptions
) {
    private val fullPath = createFilesystemFolders()


    fun getOutputPath(): Path {
        return fullPath ?: throw IllegalStateException("`outputDirectory` not set")
    }

    /**
     * Will create all required folders to output code.
     * If no outputDirectory is set in the options this method will to nothing.
     */
    private fun createFilesystemFolders(): Path? {
        val directory = options.outputDirectory
        if (directory != null) {
            // Ensure the existence of the base output directory.
            Files.createDirectories(directory)

            val fullPackagePath = directory.resolve(packageToPath(options.generatedFilesPackage))

            // Create the package directories.
            Files.createDirectories(fullPackagePath)

            return fullPackagePath
        }
        return null
    }

    /**
     * Will convert a package string to a [Path].
     * E.g. package: `com.auritylab.graphql.kotlin.codegen` will convert to `com/auritylab/graphql/kotlin/codegen`.
     */
    private fun packageToPath(`package`: String): Path {
        val stringPath = `package`.replace(".", "/")

        return Path.of(URI(stringPath))
    }
}
