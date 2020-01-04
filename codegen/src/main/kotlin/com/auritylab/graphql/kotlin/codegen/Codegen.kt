package com.auritylab.graphql.kotlin.codegen

import com.auritylab.graphql.kotlin.codegen.generator.*
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mapper.NameMapper
import graphql.schema.GraphQLEnumType
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
class Codegen(
        options: CodegenOptions = CodegenOptions(),
        schemas: List<Path>
) {
    private val schema = parseSchemas(schemas)
    private val nameMapper = NameMapper(options)
    private val kotlinTypeMapper = KotlinTypeMapper(options, nameMapper, schema)
    private val output = CodegenOutput(options)

    val enumGenerator = EnumGenerator(options, kotlinTypeMapper, nameMapper)
    val fieldResolverGenerator = FieldResolverGenerator(options, kotlinTypeMapper, nameMapper)
    val inputObjectGenerator = InputObjectGenerator(options, kotlinTypeMapper, nameMapper)
    val inputObjectParserGenerator = InputObjectParserGenerator(options, kotlinTypeMapper, nameMapper)
    val valueWrapperGenerator = ValueWrapperGenerator(options, kotlinTypeMapper, nameMapper)

    init {
        val allTypes = schema.allTypesAsList

    allTypes.filterIsInstance<GraphQLEnumType>().forEach {enumGenerator.getEnum(it).writeTo(output.getOutputPath())}
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
