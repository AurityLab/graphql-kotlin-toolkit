package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.jpa._TestUtils
import org.junit.jupiter.api.Test

internal class GQLEntityGraphBuilderTest {
    @Test
    fun shouldGenerateGraphCorrectly() {
        val selection = _TestUtils.resolveSelection()

        /*val builder = GQLEntityGraphBuilder(schema, null)

        builder.build(String::class, selection!!, schema.getType("User") as GraphQLFieldsContainer)*/
    }
}
