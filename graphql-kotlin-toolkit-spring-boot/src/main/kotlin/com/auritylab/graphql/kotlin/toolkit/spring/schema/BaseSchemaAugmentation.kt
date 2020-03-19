package com.auritylab.graphql.kotlin.toolkit.spring.schema

import com.auritylab.graphql.kotlin.toolkit.spring.schema.pagination.PaginationSchemaAugmentation
import graphql.schema.GraphQLSchema

class BaseSchemaAugmentation {
    private val delegates = listOf<SchemaAugmentation>(PaginationSchemaAugmentation())

    fun augmentSchema(schema: GraphQLSchema): GraphQLSchema {

        var cSchema = schema
        var cBuilder = GraphQLSchema.newSchema(cSchema)

        println("Previous: " + cSchema.additionalTypes.size)
        delegates.forEach {
            it.augmentSchema(cSchema, cBuilder)

            cSchema = cBuilder.build()
            cBuilder = GraphQLSchema.newSchema(cSchema)
        }


        cSchema = cBuilder.build()
        println("After: " + cSchema.additionalTypes.size)

        return cSchema
    }
}
