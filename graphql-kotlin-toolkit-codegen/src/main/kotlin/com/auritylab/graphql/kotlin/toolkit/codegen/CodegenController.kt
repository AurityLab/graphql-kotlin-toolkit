package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FileGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.GeneratorFactory
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.DirectiveHelper
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType

/**
 * Will build the [FileGenerator]s for the given [types] using the [generatorFactory].
 */
internal class CodegenController(
    private val options: CodegenOptions,
    private val types: List<GraphQLType>,
    private val generatorFactory: GeneratorFactory
) {
    /**
     * Will build all [FileGenerator]s which are needed for the given [types].
     */
    fun buildGenerators(): Collection<FileGenerator> {
        return listOf(
            buildEnumGenerators(),
            buildInputObjectGenerators(),
            buildObjectTypeGenerators(),
            buildInterfaceTypeGenerators(),
            buildAdditionalGenerators()
        ).flatten()
    }

    /**
     * Will build the generators for all [GraphQLEnumType].
     */
    private fun buildEnumGenerators(): Collection<FileGenerator> =
        types.filterIsInstance<GraphQLEnumType>()
            .map { generatorFactory.enum(it) }

    /**
     * Will build the generators for all [GraphQLInputObjectType].
     */
    private fun buildInputObjectGenerators(): Collection<FileGenerator> =
        types.filterIsInstance<GraphQLInputObjectType>()
            .map { generatorFactory.inputObject(it) }

    /**
     * Will build the generators for all [GraphQLObjectType].
     */
    private fun buildObjectTypeGenerators(): Collection<FileGenerator> =
        types.filterIsInstance<GraphQLObjectType>()
            .flatMap { objectType ->
                val internalGenerators = mutableListOf<FileGenerator>()

                val objectHasGenerate = DirectiveHelper.hasGenerate(objectType)
                val objectHasRepresentation = DirectiveHelper.getRepresentationClass(objectType) != null
                val objectHasResolver = DirectiveHelper.hasResolver(objectType)

                // Generate for object type if the directive is given and does not have a representation class.
                if ((options.generateAll && !objectHasRepresentation) || (objectHasGenerate && !objectHasRepresentation))
                    internalGenerators.add(generatorFactory.objectType(objectType))

                objectType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        objectHasResolver ||
                        DirectiveHelper.hasResolver(fieldDefinition)
                    ) internalGenerators.add(generatorFactory.fieldResolver(objectType, fieldDefinition))
                }

                return@flatMap internalGenerators
            }

    /**
     * Will build the generators for all [GraphQLInterfaceType].
     */
    private fun buildInterfaceTypeGenerators(): Collection<FileGenerator> =
        types.filterIsInstance<GraphQLInterfaceType>()
            .flatMap { interfaceType ->
                val internalGenerators = mutableListOf<FileGenerator>()

                val generatedForInterface = DirectiveHelper.hasResolver(interfaceType)

                interfaceType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        generatedForInterface ||
                        DirectiveHelper.hasResolver(fieldDefinition)
                    ) internalGenerators.add(generatorFactory.fieldResolver(interfaceType, fieldDefinition))
                }

                return@flatMap internalGenerators
            }

    /**
     * Will build the generators for all additionally required types.
     */
    private fun buildAdditionalGenerators(): Collection<FileGenerator> {
        return listOf(
            generatorFactory.valueWrapper()
        )
    }
}
