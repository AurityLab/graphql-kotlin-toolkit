package com.auritylab.graphql.kotlin.toolkit.spring.schema.pagination

import com.auritylab.graphql.kotlin.toolkit.spring.schema.SchemaTypeGenerator
import graphql.Scalars
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType

class PaginationPageInfoTypeGenerator : SchemaTypeGenerator {
    override fun generateTypes(): Collection<GraphQLType> {
        return listOf(
            GraphQLObjectType.newObject()
                .name("PageInfo")
                .field {
                    it.name("hasPreviousPage")
                    it.type(Scalars.GraphQLBoolean)
                }
                .field {
                    it.name("hasNextPage")
                    it.type(Scalars.GraphQLBoolean)
                }
                .field {
                    it.name("startCursor")
                    it.type(Scalars.GraphQLString)
                }
                .field {
                    it.name("endCursor")
                    it.type(Scalars.GraphQLString)
                }
                .build()
        )
    }
}
