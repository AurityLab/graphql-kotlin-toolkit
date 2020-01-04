package com.auritylab.graphql.kotlin.codegen

import com.auritylab.graphql.kotlin.codegen.generator.EnumGenerator
import com.auritylab.graphql.kotlin.codegen.generator.FieldResolverGenerator
import com.auritylab.graphql.kotlin.codegen.generator.InputObjectGenerator
import com.auritylab.graphql.kotlin.codegen.generator.ValueWrapperGenerator
import com.auritylab.graphql.kotlin.codegen.helper.KotlinGenerateHelper
import com.auritylab.graphql.kotlin.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.codegen.mapper.KotlinTypeMapper
import com.auritylab.graphql.kotlin.codegen.mock.WiringFactoryMock
import graphql.schema.*
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the base class for the code generation.
 *
 * directive @kotlinRepresentation(class: String!) on OBJECT | SCALAR
 * directive @kotlinGenerate on FIELD_DEFINITION | ENUM
 */
class Codegen(
        inputOptions: CodegenOptions = CodegenOptions()
) {
    internal val options = mapOptions(inputOptions)
    internal val schema = parseSchemas(options.schemas)
    internal val nameMapper = GeneratedMapper(options)
    internal val kotlinTypeMapper = KotlinTypeMapper(options, nameMapper)
    internal val outputDirectory = getOutputDirectory()

    internal val enumGenerator = EnumGenerator(options, kotlinTypeMapper, nameMapper)
    internal val fieldResolverGenerator = FieldResolverGenerator(options, kotlinTypeMapper, nameMapper)
    internal val inputObjectGenerator = InputObjectGenerator(options, kotlinTypeMapper, nameMapper)
    internal val valueWrapperGenerator = ValueWrapperGenerator(options, kotlinTypeMapper, nameMapper)

    /**
     * Will generate code for the types of the [schema].
     */
    fun generate() {
        // Get all registered types.
        val allTypes = schema.allTypesAsList

        // Create code for all enum types.
        allTypes.filterIsInstance<GraphQLEnumType>()
                .forEach { enumGenerator.getEnum(it).writeTo(outputDirectory) }

        // Will create code for all input object types.
        allTypes.filterIsInstance<GraphQLInputObjectType>()
                .forEach {
                    inputObjectGenerator.getInputObject(it)
                            .writeTo(outputDirectory)
                }

        // Will create code for all object types.
        allTypes.filterIsInstance<GraphQLObjectType>()
                .forEach { objectType ->
                    val generatedForObject = KotlinGenerateHelper.shouldGenerate(objectType)

                    objectType.fieldDefinitions.forEach { fieldDefinition ->
                        if (generatedForObject || KotlinGenerateHelper.shouldGenerate(fieldDefinition))
                            fieldResolverGenerator.getFieldResolver(objectType, fieldDefinition)
                                    .writeTo(outputDirectory)
                    }
                }

        // Will create code for all interface types.
        allTypes.filterIsInstance<GraphQLInterfaceType>()
                .forEach {interfaceType ->
                    val generatedForInterface = KotlinGenerateHelper.shouldGenerate(interfaceType)

                    interfaceType.fieldDefinitions.forEach { fieldDefinition ->
                        if (generatedForInterface || KotlinGenerateHelper.shouldGenerate(fieldDefinition))
                            fieldResolverGenerator.getFieldResolver(interfaceType, fieldDefinition)
                                    .writeTo(outputDirectory)
                    }
                }

        // Will create code for the value wrapper.
        valueWrapperGenerator.getValueWrapper()
                .writeTo(outputDirectory)
    }

    /**
     * Will parse the given [schemaFiles] and return a [GraphQLSchema] which contains all
     * registered definitions from the given schemas.
     */
    private fun parseSchemas(schemaFiles: Collection<Path>): GraphQLSchema {
        val parser = SchemaParser()
        val generator = SchemaGenerator()

        // Create a empty registry.
        val baseRegistry = TypeDefinitionRegistry()

        // Parse each schema and merge it with the base registry.
        schemaFiles.forEach {
            baseRegistry.merge(parser.parse(it.toFile()))
        }

        val genOptions = SchemaGenerator.Options.defaultOptions().enforceSchemaDirectives(false)

        return generator.makeExecutableSchema(genOptions, baseRegistry, RuntimeWiring.newRuntimeWiring().wiringFactory(WiringFactoryMock()).build())
    }

    /**
     * Will return the output directory [Path] from the [options].
     * This method will also ensure that the directories exist.
     */
    private fun getOutputDirectory(): Path {
        val directory = options.outputDirectory

        // Ensure the existence of the base output directory.
        Files.createDirectories(directory)

        return directory
    }

    private fun mapOptions(inputOptions: CodegenOptions): CodegenInternalOptions {
        val schemaFiles = inputOptions.schemas
                ?: throw IllegalArgumentException("'schemaFiles' not set")
        val outputDirectory = inputOptions.outputDirectory
                ?: throw IllegalArgumentException("'outputDirectory' not set")
        val generatedGlobalPrefix = inputOptions.generatedGlobalPrefix
        val generatedBasePackage = inputOptions.generatedBasePackage ?: "graphql.kotlin.toolkit.codegen"

        return CodegenInternalOptions(schemaFiles, outputDirectory, generatedGlobalPrefix, generatedBasePackage)
    }
}
