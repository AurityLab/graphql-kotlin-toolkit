package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen.mock.WiringFactoryMock
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.nio.file.Path

/**
 * Represents a parser which takes the input schemas from the [CodegenOptions] and create a [GraphQLSchema]
 */
class CodegenSchemaParser(
    private val options: CodegenOptions
) {
    private val parser = SchemaParser()
    private val generator = SchemaGenerator()

    /**
     * Takes the given [files] (which shall be GraphQL schema files) and create a executable [GraphQLSchema].
     */
    fun parseSchemas(files: Collection<Path>): GraphQLSchema {
        // Create a empty registry.
        val baseRegistry = TypeDefinitionRegistry()

        // Go through each given schema file, parse the schema and merge it with the base registry.
        files.forEach {
            baseRegistry.merge(parser.parse(it.toFile()))
        }

        // Create a executable schema with the create registry and the mocked WiringFactory.
        return generator.makeExecutableSchema(
            baseRegistry,
            RuntimeWiring.newRuntimeWiring().wiringFactory(WiringFactoryMock()).build()
        )
    }
}
