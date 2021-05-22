package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import graphql.schema.GraphQLNamedSchemaElement
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLType

/**
 * Object which implements various helpers for the naming of [GraphQLType].
 */
object GraphQLNameHelper {
    /**
     * Will build a readable name for the given [type].
     */
    fun buildReadableName(type: GraphQLType): String {
        return when (type) {
            is GraphQLNamedType -> type.name
            else -> type.toString()
        }
    }

    /**
     * WIll build a readable name for the given [element].
     */
    fun buildReadableName(element: GraphQLSchemaElement): String {
        return when (element) {
            is GraphQLNamedSchemaElement -> element.name
            else -> element.toString()
        }
    }
}

/**
 * @see GraphQLNameHelper.buildReadableName
 */
fun GraphQLType.toReadableName(): String {
    return GraphQLNameHelper.buildReadableName(this)
}

/**
 * @see GraphQLNameHelper.buildReadableName
 */
fun GraphQLSchemaElement.toReadableName(): String {
    return GraphQLNameHelper.buildReadableName(this)
}
