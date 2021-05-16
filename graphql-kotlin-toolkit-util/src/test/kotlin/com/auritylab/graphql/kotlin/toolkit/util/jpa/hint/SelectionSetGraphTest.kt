package com.auritylab.graphql.kotlin.toolkit.util.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.util.jpa._TestUtils
import graphql.schema.GraphQLFieldsContainer
import org.junit.jupiter.api.Test

internal class SelectionSetGraphTest {
    @Test
    fun shouldCreateSelectionSetGraphCorrectly() {
        val schema = _TestUtils.createSchema()
        val selection = _TestUtils.resolveSelection()

        val selections =
            SelectionSetGraph(FullHintGraph(schema), selection, schema.getType("User") as GraphQLFieldsContainer)

        println(selections)
    }
}
