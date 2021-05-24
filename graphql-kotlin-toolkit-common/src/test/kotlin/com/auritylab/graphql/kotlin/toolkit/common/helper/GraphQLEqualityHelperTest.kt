package com.auritylab.graphql.kotlin.toolkit.common.helper

import graphql.Scalars
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GraphQLEqualityHelperTest {
    @Test
    fun `should compare same scalars properly`() {
        assertTrue(GraphQLEqualityHelper.isEqual(Scalars.GraphQLString, Scalars.GraphQLString))
    }

    @Test
    fun `should compare non null wrapped scalars properly`() {
        assertTrue(
            GraphQLEqualityHelper.isEqual(
                GraphQLNonNull(Scalars.GraphQLString),
                GraphQLNonNull(Scalars.GraphQLString)
            )
        )
    }

    @Test
    fun `should compare list wrapped scalars properly`() {
        assertTrue(
            GraphQLEqualityHelper.isEqual(
                GraphQLList(Scalars.GraphQLString),
                GraphQLList(Scalars.GraphQLString),
            )
        )
    }

    @Test
    fun `should compare list wrapped object type properly`() {
        val testObject = GraphQLObjectType.newObject()
            .name("Test")
            .field {
                it.name("name")
                it.type(Scalars.GraphQLString)
            }
            .build()

        assertTrue(GraphQLEqualityHelper.isEqual(GraphQLList(testObject), GraphQLList(testObject)))
    }

    @Test
    fun `should compare one list wrapped type with non list wrapped type properly`() {
        assertFalse(GraphQLEqualityHelper.isEqual(GraphQLList(Scalars.GraphQLString), Scalars.GraphQLString))
    }
}
