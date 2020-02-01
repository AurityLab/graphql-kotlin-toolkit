package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema

/**
 * Maps the [GraphQLInterfaceType] to their implementors.
 */
internal class ImplementerMapper(
    private val options: CodegenOptions,
    private val schema: GraphQLSchema
) {
    /**
     * Will search for all implementers for the given [GraphQLInterfaceType] and return them.
     */
    fun getImplementers(input: GraphQLInterfaceType): Collection<GraphQLObjectType> {
        return schema.allTypesAsList
            .filterIsInstance<GraphQLObjectType>()
            .filter { it != input }
            .filter { schema.isPossibleType(input, it) }
    }
}
