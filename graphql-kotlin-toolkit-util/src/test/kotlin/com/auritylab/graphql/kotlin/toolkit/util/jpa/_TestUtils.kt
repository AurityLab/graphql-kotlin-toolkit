package com.auritylab.graphql.kotlin.toolkit.util.jpa

import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingFieldSelectionSet
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
    private fun loadSchema(): String =
        javaClass.classLoader.getResourceAsStream("schema.graphqls")?.reader()?.readText()
            ?: throw IllegalStateException("Schema not found")

    /**
     * Will load the "query.graph" file from the resources. If the file was not found on the resources, an exception
     * will be thrown. The file can hold multiple operations, therefore we only need to load one file here.
     */
    private fun loadQuery(): String = javaClass.classLoader.getResourceAsStream("query.graphql")?.reader()?.readText()
        ?: throw IllegalStateException("Query not found")

    /**
     * Will create a [GraphQLSchema] based on the schema which will be loaded through [loadSchema]. The wiring for the
     * schema will be initialized using the given [dataFetchers].
     */
    fun createSchema(dataFetchers: Map<FieldCoordinates, DataFetcher<out Any>>): GraphQLSchema {
        val codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
        dataFetchers.forEach { (key, value) -> codeRegistry.dataFetcher(key, value) }

        val wiring = RuntimeWiring.newRuntimeWiring()
            .codeRegistry(codeRegistry)
            .build()

        return SchemaGenerator().makeExecutableSchema(SchemaParser().parse(loadSchema()), wiring)
    }

    /**
     * Will create a [GraphQLSchema] based on the schema which will be loaded through [loadSchema]. This will be
     * created using an empty runtime wiring.
     */
    fun createSchema(): GraphQLSchema {
        return createSchema(mapOf())
    }

    fun resolveSelection(): DataFetchingFieldSelectionSet {
        var selection: DataFetchingFieldSelectionSet? = null

        val schema = createSchema(
            mapOf(
                FieldCoordinates.coordinates("Query", "getUsers") to DataFetcher { env ->
                    selection = env.selectionSet
                    listOf<Any>()
                }
            )
        )
        val gql = GraphQL.newGraphQL(schema).build()

        gql.execute(ExecutionInput.newExecutionInput().query(loadQuery()))

        return selection!!
    }
}
