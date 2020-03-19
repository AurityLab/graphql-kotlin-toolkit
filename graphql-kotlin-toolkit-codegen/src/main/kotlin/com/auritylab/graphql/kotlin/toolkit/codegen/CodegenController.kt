package com.auritylab.graphql.kotlin.toolkit.codegen

import com.auritylab.graphql.kotlin.toolkit.codegen.generator.FileGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.generator.GeneratorFactory
import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
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
            buildAdditionalGenerators(),
            buildPaginationGenerators()
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

                val objectHasGenerate = DirectiveFacade.generate[objectType]
                val objectHasRepresentation = DirectiveFacade.representation[objectType]
                val objectHasResolver = DirectiveFacade.resolver[objectType]

                // Generate for object type if the directive is given and does not have a representation class.
                if ((options.generateAll && !objectHasRepresentation) || (objectHasGenerate && !objectHasRepresentation))
                    internalGenerators.add(generatorFactory.objectType(objectType))

                objectType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        objectHasResolver ||
                        DirectiveFacade.resolver[fieldDefinition]
                    ) internalGenerators.add(getSwitchedFieldResolverGenerator(objectType, fieldDefinition))
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

                val generatedForInterface = DirectiveFacade.resolver[interfaceType]

                interfaceType.fieldDefinitions.forEach { fieldDefinition ->
                    if (options.generateAll ||
                        generatedForInterface ||
                        DirectiveFacade.resolver[fieldDefinition]
                    ) internalGenerators.add(getSwitchedFieldResolverGenerator(interfaceType, fieldDefinition))
                }

                return@flatMap internalGenerators
            }

    /**
     * Will build the field resolver [FileGenerator] for the given [container] and [field].ø
     * This will differ if the given field has pagination or not.
     *
     * @return The default FieldResolver or the PaginationFieldResolver.
     */
    private fun getSwitchedFieldResolverGenerator(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): FileGenerator = if (DirectiveFacade.pagination[field] && GraphQLTypeHelper.isList(field.type))
        generatorFactory.paginationFieldResolver(container, field)
    else
        generatorFactory.fieldResolver(container, field)

    /**
     * Will build the generators for all additionally required types.
     */
    private fun buildAdditionalGenerators(): Collection<FileGenerator> {
        return listOf(
            generatorFactory.valueWrapper()
        )
    }

    /**
     * Will build the generators for the pagination support.
     */
    private fun buildPaginationGenerators(): Collection<FileGenerator> {
        return listOf(
            generatorFactory.paginationInfo(),
            generatorFactory.paginationConnection(),
            generatorFactory.paginationEdge(),
            generatorFactory.paginationPageInfo()
        )
    }
}
