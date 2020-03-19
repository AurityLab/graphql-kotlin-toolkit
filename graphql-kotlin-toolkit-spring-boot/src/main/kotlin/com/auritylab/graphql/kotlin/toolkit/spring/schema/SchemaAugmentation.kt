package com.auritylab.graphql.kotlin.toolkit.spring.schema

import graphql.schema.GraphQLSchema

interface SchemaAugmentation {
    fun augmentSchema(existingSchema: GraphQLSchema, transform: GraphQLSchema.Builder)
}
