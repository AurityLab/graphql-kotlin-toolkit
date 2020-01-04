package com.auritylab.graphql.kotlin.codegen

import com.auritylab.graphql.kotlin.codegen.generator.*
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.nio.file.Path

/**
 * directive @kotlinRepresentation(class: String!) on OBJECT | SCALAR
 * directive @kotlinGenerate on FIELD_DEFINITION | ENUM
 */
class Poet(
        options: PoetOptions = PoetOptions(),
        schemas: List<Path>
) {
    val schema = parseSchemas(schemas)
    val nameMapper = NameMapper(options)
    val kotlinTypeMapper = KotlinTypeMapper(options, nameMapper, schema)

    val enumGenerator = EnumGenerator(options, kotlinTypeMapper, nameMapper)
    val fieldResolverGenerator = FieldResolverGenerator(options, kotlinTypeMapper, nameMapper)
    val inputObjectGenerator = InputObjectGenerator(options, kotlinTypeMapper, nameMapper)
    val inputObjectParserGenerator = InputObjectParserGenerator(options, kotlinTypeMapper, nameMapper)
    val valueWrapperGenerator = ValueWrapperGenerator(options, kotlinTypeMapper, nameMapper)

    init {
    }

    /**
     * Will parse the given [schemaFiles] and return a [TypeDefinitionRegistry] which contains all
     * registered definitions from the given schemas.
     */
    private fun parseSchemas(schemaFiles: List<Path>): GraphQLSchema {
        val parser = SchemaParser()
        val generator = SchemaGenerator()

        // Create a empty registry.
        val baseRegistry = TypeDefinitionRegistry()

        // Parse each schema and merge it with the base registry.
        schemaFiles.forEach {
            baseRegistry.merge(parser.parse(it.toFile()))
        }

        return generator.makeExecutableSchema(baseRegistry, RuntimeWiring.newRuntimeWiring().build())
    }
}
