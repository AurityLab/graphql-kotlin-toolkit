package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

object GraphQLTypeHelper {
    /**
     * Will unwrap the given [type] to the actual type. This is necessary because a incoming type can be wrapped in a
     * [ListType] or a [NonNullType].
     */
    fun unwrapType(type: GraphQLModifiedType): GraphQLType {
        return when (type) {
            is GraphQLList -> type.wrappedType
            is GraphQLNonNull -> type.wrappedType
            else -> type
        }
    }

    /**
     * Will recursively unwrap the given [type] using [unwrapType]. A type can be wrapped multiple types.
     */
    fun unwrapTypeFull(type: GraphQLType): GraphQLType {
        // If it's no modified type unwrapping is not necessary.
        if (type !is GraphQLModifiedType)
            return type

        var t: GraphQLType = type
        // Will unwrap until the Type is TypeName.ø
        while (t is GraphQLModifiedType)
            t = unwrapType(t)

        return t
    }
}
