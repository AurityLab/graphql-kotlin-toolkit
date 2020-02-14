package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.EnumGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.EnvironmentWrapperGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FieldResolverGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.InputObjectGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.ObjectTypeGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.ValueWrapperGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.DirectiveHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.ImplementerMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.FileSpec
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the base class for the code generation.
 */
class Codegen(
    private val options: CodegenOptions
) {
    private val schema = CodegenSchemaParser(options).parseSchemas(options.schemas)
    private val nameMapper = GeneratedMapper(options)
    private val kotlinTypeMapper = KotlinTypeMapper(options, nameMapper)
    private val implementerMapper = ImplementerMapper(options, schema)
    private val outputDirectory = getOutputDirectory()

    private val argumentCodeBlockGenerator = ArgumentCodeBlockGenerator(kotlinTypeMapper, nameMapper)
    private val enumGenerator = EnumGenerator(options, kotlinTypeMapper, nameMapper)
    private val fieldResolverGenerator =
        FieldResolverGenerator(options, kotlinTypeMapper, implementerMapper, nameMapper, argumentCodeBlockGenerator)
    private val inputObjectGenerator =
        InputObjectGenerator(options, kotlinTypeMapper, nameMapper, argumentCodeBlockGenerator)
    private val valueWrapperGenerator = ValueWrapperGenerator(options, kotlinTypeMapper, nameMapper)
    private val environmentWrapperGenerator = EnvironmentWrapperGenerator(options, kotlinTypeMapper, nameMapper)
    private val objectTypeGenerator = ObjectTypeGenerator(options, kotlinTypeMapper, nameMapper)

    /**
     * Will generate code for the types of the [schema].
     */
    fun generate() {
        // Get all registered types.
        val allTypes = schema.allTypesAsList

        // Create code for all enum types.
        allTypes.filterIsInstance<GraphQLEnumType>()
            .forEach { write(enumGenerator.getEnum(it)) }

        // Will create code for all input object types.
        allTypes.filterIsInstance<GraphQLInputObjectType>()
            .forEach { write(inputObjectGenerator.getInputObject(it)) }

        // Will create code for all object types.
        allTypes.filterIsInstance<GraphQLObjectType>()
            .forEach { objectType ->
                val objectHasGenerate = DirectiveHelper.hasGenerate(objectType)
                val objectHasRepresentation = DirectiveHelper.getRepresentationClass(objectType) != null
                val objectHasResolver = DirectiveHelper.hasResolver(objectType)

                // Generate for object type if the directive is given and does not have a representation class.
                if ((options.generateAll && !objectHasRepresentation) || (objectHasGenerate && !objectHasRepresentation))
                    write(objectTypeGenerator.getObjectType(objectType))

                objectType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        objectHasResolver ||
                        DirectiveHelper.hasResolver(fieldDefinition)
                    )
                        write(fieldResolverGenerator.getFieldResolver(objectType, fieldDefinition))
                }
            }

        // Will create code for all interface types.
        allTypes.filterIsInstance<GraphQLInterfaceType>()
            .forEach { interfaceType ->
                val generatedForInterface = DirectiveHelper.hasResolver(interfaceType)

                interfaceType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        generatedForInterface ||
                        DirectiveHelper.hasResolver(fieldDefinition)
                    )
                        write(fieldResolverGenerator.getFieldResolver(interfaceType, fieldDefinition))
                }
            }

        // Will create code for the value wrapper.
        write(valueWrapperGenerator.getValueWrapper())

        // Will create code for the environment wrapper.
        write(environmentWrapperGenerator.getEnvironmentWrapper())
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

    /**
     * Will write the given [spec] to the [outputDirectory].
     */
    private fun write(spec: FileSpec) =
        spec.writeTo(outputDirectory)
}
