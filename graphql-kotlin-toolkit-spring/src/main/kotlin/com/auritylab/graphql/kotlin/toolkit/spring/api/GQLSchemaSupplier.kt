package com.auritylab.graphql.kotlin.toolkit.spring.api

import org.springframework.core.io.DefaultResourceLoader

/**
 * Describes a supplier for multiple GraphQL schemas.
 */
class GQLSchemaSupplier private constructor(strings: Collection<String>) {
    internal val schemaStrings: Collection<String> = strings

    companion object {
        private val resourceLoader = DefaultResourceLoader()

        /**
         * Will use the given [strings] as schemas.
         */
        fun ofStrings(strings: Collection<String>): GQLSchemaSupplier =
            GQLSchemaSupplier(strings)

        /**
         * Will use the given [strings] as schema.
         */
        fun ofStrings(vararg strings: String): GQLSchemaSupplier =
            ofStrings(strings.asList())

        /**
         * Will resolve the given [files] and use their content as schemas.
         */
        fun ofResourceFiles(files: Collection<String>): GQLSchemaSupplier =
            GQLSchemaSupplier(files.map { resolveResourceFile(it) })

        /**
         * Will resolve the given [files] and use their content as schemas.
         */
        fun ofResourceFiles(vararg files: String): GQLSchemaSupplier =
            ofResourceFiles(files.asList())

        /**
         * Will search for the given [file] on the classpath.
         * If the [file] was found the content will be returned.
         */
        private fun resolveResourceFile(file: String): String {
            val resource = resourceLoader.getResource(file)

            if (!resource.exists())
                throw IllegalArgumentException("Schema file '$file' could not be found")

            // Read the content of the file and return it.
            return resource.inputStream.reader(Charsets.UTF_8).readText()
        }
    }
}
