package com.auritylab.graphql.kotlin.toolkit.spring.schema.pagination

import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.spring.schema.SchemaAugmentation
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLTypeReference

class PaginationSchemaAugmentation : SchemaAugmentation {
    override fun augmentSchema(existingSchema: GraphQLSchema, schema: GraphQLSchema.Builder) {

        val paginatedTypes = mutableListOf<GraphQLOutputType>()

        val augmentedTypes = existingSchema.additionalTypes
            .map { type ->
                if (type !is GraphQLObjectType)
                    return@map type

                val result = mapObjectType(type)
                paginatedTypes.addAll(result.second)
                result.first
            }

        schema.query(mapObjectType(existingSchema.queryType).let { paginatedTypes.addAll(it.second);it.first })
        schema.mutation(mapObjectType(existingSchema.mutationType).let { paginatedTypes.addAll(it.second);it.first })

        schema.clearAdditionalTypes()
        schema.additionalTypes(augmentedTypes.toSet())

        println(paginatedTypes)
        if (paginatedTypes.isNotEmpty()) {
            schema.additionalTypes(PaginationPageInfoTypeGenerator().generateTypes().toSet())

            paginatedTypes.forEach { type ->
                if (type is GraphQLObjectType)
                    schema.additionalTypes(PaginationTypesGenerator(type).generateTypes().toSet())
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

    private fun getConnectionType(input: GraphQLOutputType): GraphQLOutputType {
        return GraphQLTypeReference(input.name + "Connection")
    }

    private fun mapObjectType(type: GraphQLObjectType): Pair<GraphQLObjectType, Collection<GraphQLOutputType>> {
        val paginationTypes = mutableListOf<GraphQLOutputType>()

        return Pair(type.transform { trans ->
            val augmentedFields = type.fieldDefinitions.map { field ->
                if (!DirectiveFacade.pagination[field])
                    field
                else {
                    paginationTypes.add(field.type)
                    field.transform {
                        it.type(getConnectionType(field.type))
                    }
                }
            }

            trans.clearFields()
            trans.fields(augmentedFields)
        }, paginationTypes)
    }
}
