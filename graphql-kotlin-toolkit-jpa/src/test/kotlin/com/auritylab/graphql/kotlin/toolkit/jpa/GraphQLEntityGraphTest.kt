package com.auritylab.graphql.kotlin.toolkit.jpa

import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLTypeVisitorStub
import graphql.schema.SchemaTraverser
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import org.junit.jupiter.api.Test

internal class GraphQLEntityGraphTest {
    @Test
    fun test() {
        val schema = _TestUtils.createSchema(DataFetcher { "" }, DataFetcher { "" })

        SchemaTraverser().depthFirst(Trav, schema.queryType)
    }

    object Trav : GraphQLTypeVisitorStub() {
        override fun visitGraphQLFieldDefinition(
            node: GraphQLFieldDefinition,
            context: TraverserContext<GraphQLSchemaElement>
        ): TraversalControl {
            val parent = context.parentNode
            if (parent is GraphQLObjectType)
                println("parent: ${parent.name}")
            println("node: ${node.name}")

            return TraversalControl.CONTINUE
        }
    }
}
