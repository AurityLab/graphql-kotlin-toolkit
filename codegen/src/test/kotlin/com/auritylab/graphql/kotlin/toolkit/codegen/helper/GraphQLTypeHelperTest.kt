package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import kotlin.reflect.KClass

internal class GraphQLTypeHelperTest : StringSpec({
    "Should unwrap correctly" {
        forall(
            row(
                createDummyWrapper(objectType, GraphQLList::class),
                objectType
            ),
            row(
                createDummyWrapper(objectType, GraphQLList::class, GraphQLList::class, GraphQLList::class),
                objectType
            ),
            row(
                createDummyWrapper(objectType, GraphQLList::class, GraphQLNonNull::class, GraphQLList::class),
                objectType
            ),
            row(
                createDummyWrapper(
                    objectType,
                    GraphQLNonNull::class
                ), objectType
            )
        ) { a, b -> GraphQLTypeHelper.unwrapType(a).shouldBe(b) }
    }

    "Should wrap correctly" {
        forall(
            row(
                createDummyWrapper(objectType, GraphQLNonNull::class, GraphQLList::class),
                LIST.parameterizedBy(ANY.copy(true))
            ),
            row(
                createDummyWrapper(
                    objectType,
                    GraphQLNonNull::class,
                    GraphQLList::class,
                    GraphQLList::class,
                    GraphQLList::class
                ),
                LIST.parameterizedBy(
                    LIST.parameterizedBy(
                        LIST.parameterizedBy(
                            ANY.copy(true)
                        ).copy(true)
                    ).copy(true)
                )
            )
        ) { a, b ->
            GraphQLTypeHelper.wrapType(a, ANY).shouldBe(b)
        }
    }
})

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
