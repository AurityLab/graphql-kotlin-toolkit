package com.auritylab.graphql.kotlin.toolkit.jpa

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser

/**
 * Utils class which holds methods to simplify testing.
 */
object _TestUtils {
    /**
     * Will load the "schema.graphqls" file from the resources. If the file was not found on the resources, an
     * exception will be thrown.
     */
    fun loadSchema(): String = javaClass.classLoader.getResourceAsStream("schema.graphqls")?.reader()?.readText()
        ?: throw IllegalStateException("Schema not found")

    /**
     * Will load the "query.graph" file from the resources. If the file was not found on the resources, an exception
     * will be thrown.
     */
    fun loadQuery(): String = javaClass.classLoader.getResourceAsStream("query.graphql")?.reader()?.readText()
        ?: throw IllegalStateException("Query not found")

    /**
     * Will create a [GraphQLSchema] based on the schema which will be loaded through [loadSchema]. This will create an
     * empty runtime wiring.
     */
    fun createSchema(getUsersDF: DataFetcher<Any>, getUserDF: DataFetcher<Any>): GraphQLSchema {
        val wiring = RuntimeWiring.newRuntimeWiring()
            .codeRegistry(
                GraphQLCodeRegistry.newCodeRegistry()
                    .dataFetcher(FieldCoordinates.coordinates("Query", "getUsers"), getUsersDF)
                    .dataFetcher(FieldCoordinates.coordinates("Query", "getUser"), getUserDF)
                    .build()
            )
            .build()

        return SchemaGenerator().makeExecutableSchema(SchemaParser().parse(loadSchema()), wiring)
    }

    /**
     * Will build a [GraphQL] instance based on the given [schema].
     */
    fun createGraphQL(schema: GraphQLSchema): GraphQL = GraphQL.newGraphQL(schema).build()
}
