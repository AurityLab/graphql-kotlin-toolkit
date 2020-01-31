package com.auritylab.graphql.kotlin.toolkit.spring.api

import org.springframework.core.io.DefaultResourceLoader

/**
 * Private implementation of a [GraphQLSchemaSupplier].
 */
private data class Data(override val schemas: Collection<String>) : GraphQLSchemaSupplier

/**
 * Will use the given [strings] as schemas.
 */
fun GraphQLSchemaSupplier.ofStrings(strings: Collection<String>): GraphQLSchemaSupplier =
    Data(strings)

/**
 * Will use the given [strings] as schema.
 */
fun GraphQLSchemaSupplier.ofStrings(vararg strings: String): GraphQLSchemaSupplier =
    ofStrings(strings.asList())

/**
 * Will resolve the given [files] and use their content as schemas.
 */
fun GraphQLSchemaSupplier.ofResourceFiles(files: Collection<String>): GraphQLSchemaSupplier =
    Data(files.map { resolveResourceFile(it) })

/**
 * Will resolve the given [files] and use their content as schemas.
 */
fun GraphQLSchemaSupplier.ofResourceFiles(vararg files: String): GraphQLSchemaSupplier =
    ofResourceFiles(files.asList())

/**
 * Will search for the given [file] on the classpath.
 * If the [file] was found the content will be returned.
 */
private fun resolveResourceFile(file: String): String {
    val resource = DefaultResourceLoader().getResource(file)

    // Check if the resource file exists.
    if (!resource.exists())
        throw IllegalArgumentException("Schema file '$file' could not be found")

    // Read the content of the file and return it.
    return resource.inputStream.reader(Charsets.UTF_8).readText()
}
