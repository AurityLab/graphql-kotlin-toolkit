package com.auritylab.graphql.kotlin.toolkit.spring.schema.pagination

import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.auritylab.graphql.kotlin.toolkit.spring.schema.SchemaAugmentation
import graphql.Scalars
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference

class PaginationSchemaAugmentation : SchemaAugmentation {
    override fun augmentSchema(existingSchema: GraphQLSchema, transform: GraphQLSchema.Builder) {

        val paginatedTypes = mutableListOf<GraphQLType>()

        val augmentedTypes = existingSchema.additionalTypes
            .map { type ->
                if (type !is GraphQLObjectType)
                    return@map type

                val result = mapObjectType(type)
                paginatedTypes.addAll(result.second)
                result.first
            }

        transform.query(mapObjectType(existingSchema.queryType).let { paginatedTypes.addAll(it.second); it.first })
        transform.mutation(mapObjectType(existingSchema.mutationType).let { paginatedTypes.addAll(it.second); it.first })

        transform.clearAdditionalTypes()
        transform.additionalTypes(augmentedTypes.toSet())

        if (paginatedTypes.isNotEmpty()) {
            transform.additionalTypes(PaginationPageInfoTypeGenerator().generateTypes().toSet())

            paginatedTypes.forEach { type ->
                if (type is GraphQLObjectType)
                    transform.additionalTypes(PaginationTypesGenerator(type).generateTypes().toSet())
            }
        }
    }

    private fun getFieldDefinitions(schema: GraphQLSchema) =
        schema.allTypesAsList
            .filterIsInstance<GraphQLObjectType>()
            .flatMap { it.fieldDefinitions }

    private fun getMatchingFieldDefinitions(
        definitions: Collection<GraphQLFieldDefinition>
    ): Collection<GraphQLFieldDefinition> =
        definitions.filter { DirectiveFacade.pagination[it] }

    private fun getConnectionType(input: GraphQLType): GraphQLOutputType {
        if (input !is GraphQLNamedType)
            throw IllegalArgumentException("Expected named type")
        return GraphQLTypeReference(input.name + "Connection")
    }

    private fun mapObjectType(type: GraphQLObjectType): Pair<GraphQLObjectType, Collection<GraphQLType>> {
        val paginationTypes = mutableListOf<GraphQLType>()

        return Pair(type.transform { trans ->
            val augmentedFields = type.fieldDefinitions.map { field ->
                if (!DirectiveFacade.pagination[field])
                    field
                else {
                    val unwrappedType = GraphQLTypeHelper.unwrapType(field.type)

                    paginationTypes.add(unwrappedType)
                    field.transform {
                        it.arguments(field.arguments.plus(buildPaginationArguments()))
                        it.type(getConnectionType(unwrappedType))
                    }
                }
            }

            trans.clearFields()
            trans.fields(augmentedFields)
        }, paginationTypes)
    }

    private fun buildPaginationArguments(): List<GraphQLArgument> {
        return listOf(
            GraphQLArgument.newArgument().name("first").type(Scalars.GraphQLInt).build(),
            GraphQLArgument.newArgument().name("after").type(Scalars.GraphQLString).build(),
            GraphQLArgument.newArgument().name("last").type(Scalars.GraphQLInt).build(),
            GraphQLArgument.newArgument().name("before").type(Scalars.GraphQLString).build()
        )
    }
}
