package com.auritylab.graphql.kotlin.toolkit.spring.schema.pagination

import com.auritylab.graphql.kotlin.toolkit.spring.schema.SchemaTypeGenerator
import graphql.Scalars
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference

class PaginationTypesGenerator(
    private val objectType: GraphQLObjectType
) : SchemaTypeGenerator {
    override fun generateTypes(): Collection<GraphQLType> {
        return listOf(
            buildConnectionType(),
            buildEdgeType()
        )
    }

    private fun buildConnectionType(): GraphQLObjectType {
        return GraphQLObjectType.newObject()
            .name(connectionTypeName)
            .field {
                it.name("pageInfo")
                it.type(GraphQLTypeReference("PageInfo"))
            }
            .field {
                it.name("edges")
                it.type(GraphQLList(GraphQLTypeReference(edgeTypeName)))
            }
            .build()
    }

    private fun buildEdgeType(): GraphQLObjectType {
        return GraphQLObjectType.newObject()
            .name(edgeTypeName)
            .field {
                it.name("node")
                it.type(objectType)
            }
            .field {
                it.name("cursor")
                it.type(Scalars.GraphQLString)
            }
            .build()
    }

    private val connectionTypeName = objectType.name + "Connection"
    private val edgeTypeName = objectType.name + "Edge"
}
