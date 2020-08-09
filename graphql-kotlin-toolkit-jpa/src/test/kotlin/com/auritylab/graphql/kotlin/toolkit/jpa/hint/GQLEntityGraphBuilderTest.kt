package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.jpa._TestUtils
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldsContainer
import org.junit.jupiter.api.Test

internal class GQLEntityGraphBuilderTest {
    @Test
    fun shouldGenerateGraphCorrectly() {
        var selection: DataFetchingFieldSelectionSet? = null

        val schema = _TestUtils.createSchema(DataFetcher { env ->
            selection = env.selectionSet
            listOf<Any>()
        }, DataFetcher<Any> { env -> null })
        val gql = _TestUtils.createGraphQL(schema)

        val execResult = gql.execute(_TestUtils.loadQuery())

        val selections =
            SelectionSetHints(FullHintGraph(schema), selection!!, schema.getType("User") as GraphQLFieldsContainer)

        /*val builder = GQLEntityGraphBuilder(schema, null)

        builder.build(String::class, selection!!, schema.getType("User") as GraphQLFieldsContainer)*/
    }
}
