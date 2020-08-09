package com.auritylab.graphql.kotlin.toolkit.jpa.hint

import com.auritylab.graphql.kotlin.toolkit.jpa._TestUtils
import graphql.schema.DataFetcher
import org.junit.jupiter.api.Test

internal class FullHintGraphTest {
    @Test
    fun shouldTraversSchemaCorrectly() {
        val schema = _TestUtils.createSchema(DataFetcher { "" }, DataFetcher { "" })

        val hints = FullHintGraph(schema)

        println(hints.graph)
    }
}
