package com.auritylab.graphql.kotlin.toolkit.spring.api

/**
 * Describes a supplier for multiple GraphQL Schema sources.
 */
interface GraphQLSchemaSupplier {
    /**
     * The GraphQL schemas as sources.
     */
    val schemas: Collection<String>
}
