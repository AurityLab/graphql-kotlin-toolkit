package com.auritylab.graphql.kotlin.toolkit.spring.schema

import graphql.schema.GraphQLType

interface SchemaTypeGenerator {
    fun generateTypes(): Collection<GraphQLType>
}
