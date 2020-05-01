package com.auritylab.graphql.kotlin.toolkit.common.helper

import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import java.util.stream.Stream
import kotlin.reflect.KClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class GraphQLTypeHelperTest {
    @Nested
    @DisplayName("unwrapType()")
    inner class UnwrapType {
        @ParameterizedTest
        @MethodSource(shouldUnwrapTypeCorrectly_source_pointer)
        fun `should unwrap type correctly`(input: GraphQLType, output: GraphQLType) {
            // Unwrap the given input type.
            val result = GraphQLTypeHelper.unwrapType(input)

            // The given input must match the expected output type.
            Assertions.assertEquals(output, result)
        }
    }

    @Nested
    @DisplayName("unwrapTypeLayers()")
    inner class UnwrapTypeLayers {
        @Test
        fun `should unwrap type layers correctly`() {
            // Unwrap a dummy type.
            val layers = GraphQLTypeHelper.unwrapTypeLayers(
                createDummyWrapper(
                    objectType,
                    GraphQLList::class,
                    GraphQLNonNull::class,
                    GraphQLList::class
                )
            )

            // Assert the size of the result.
            Assertions.assertEquals(4, layers.size)

            // Assert against each layer.
            Assertions.assertEquals(layers[0]::class, GraphQLList::class)
            Assertions.assertEquals(layers[1]::class, GraphQLNonNull::class)
            Assertions.assertEquals(layers[2]::class, GraphQLList::class)
            Assertions.assertEquals(layers[3]::class, objectType::class)
        }

        @Test
        fun `should unwrap single layer correctly`() {
            // Unwrap a type which is not wrapped at all.
            val layers = GraphQLTypeHelper.unwrapTypeLayers(objectType)

            // Should have exactly one layer.
            Assertions.assertEquals(1, layers.size)

            // Assert against the type.
            Assertions.assertEquals(layers[0], objectType)
        }
    }

    @Nested
    @DisplayName("isList()")
    inner class IsList {
        @Test
        fun `should check if type is a list correctly`() {
            val result = GraphQLTypeHelper.isList(listObjectType)

            Assertions.assertTrue(result)
        }

        @Test
        fun `should check if type (with non null wrapper) is a list correctly`() {
            val result = GraphQLTypeHelper.isList(listObjectTypeNN)

            Assertions.assertTrue(result)
        }
    }

    @Nested
    @DisplayName("getListType()")
    inner class GetListType {
        @Test
        fun `should return the wrapped type of the list correctly`() {
            val result = GraphQLTypeHelper.getListType(listObjectType)

            Assertions.assertEquals(objectType, result)
        }

        @Test
        fun `should return the wrapped non null type of the list correctly`() {
            val result = GraphQLTypeHelper.getListType(listObjectTypeNN)

            Assertions.assertEquals(objectType, result)
        }

        @Test
        fun `should return null if given type is not a list`() {
            val result = GraphQLTypeHelper.getListType(objectType)

            Assertions.assertNull(result)
        }
    }

    companion object {
        const val shouldUnwrapTypeCorrectly_source_pointer = "com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelperTest#shouldUnwrapTypeCorrectly_source"

        @JvmStatic
        fun shouldUnwrapTypeCorrectly_source(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    createDummyWrapper(objectType, GraphQLList::class),
                    objectType
                ),
                Arguments.of(
                    createDummyWrapper(objectType, GraphQLList::class, GraphQLList::class, GraphQLList::class),
                    objectType
                ),
                Arguments.of(
                    createDummyWrapper(objectType, GraphQLList::class, GraphQLNonNull::class, GraphQLList::class),
                    objectType
                )
            )
        }
    }
}

/**
 * Will create a new [GraphQLType] which is wrapped by the given [types] and the [innerType] as children of the last of [types].
 */
fun createDummyWrapper(innerType: GraphQLType, vararg types: KClass<out GraphQLModifiedType>): GraphQLType {
    return types.foldRight(innerType) { kClass, acc ->
        createModifiedTypeInstance(kClass, acc)!!
    }
}

/**
 * Will create a new instance of the given [type] and the given [children] as wrapped type.
 */
fun createModifiedTypeInstance(type: KClass<out GraphQLModifiedType>, children: GraphQLType): GraphQLModifiedType? {
    return when (type) {
        GraphQLList::class -> GraphQLList(children)
        GraphQLNonNull::class -> GraphQLNonNull(children)
        else -> null
    }
}

private val objectType = GraphQLObjectType.newObject().name("test").build()
private val listObjectType = GraphQLList(objectType)
private val listObjectTypeNN = GraphQLNonNull(GraphQLList(objectType))
