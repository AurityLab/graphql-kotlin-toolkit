package com.auritylab.graphql.kotlin.codegen

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

            return directory
        }
        return null
    }
}
