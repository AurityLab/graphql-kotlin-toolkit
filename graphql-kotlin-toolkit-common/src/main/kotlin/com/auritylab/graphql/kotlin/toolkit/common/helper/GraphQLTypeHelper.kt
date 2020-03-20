package com.auritylab.graphql.kotlin.toolkit.common.helper

import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

object GraphQLTypeHelper {
    /**
     * Will unwrap the given [type] until the type is no longer instance of [GraphQLModifiedType].
     * A [GraphQLType] can wrapped multiple times with a [GraphQLModifiedType].
     */
    fun unwrapType(type: GraphQLType): GraphQLType {
        // If it's no modified type unwrapping is not necessary.
        if (type !is GraphQLModifiedType)
            return type

        var t = type
        // Will unwrap until the Type is no modified type.
        while (t is GraphQLModifiedType)
            t = t.wrappedType

        return t
    }

    /**
     * Will check if the given [GraphQLType] is a List. This will also check if the first layer is a [GraphQLNonNull].
     *
     * @param type The type to check against.
     * @return If the given type is [GraphQLList].
     */
    fun isList(type: GraphQLType): Boolean =
        getListType(type) != null

    /**
     * Will check if the given [type] is a List. If it is a list, it will return the wrapped type of the list.
     *
     * @param type The type which is expected to be a list.
     * @return The wrapped type of the list or null if [type] is no list.
     */
    fun getListType(type: GraphQLType): GraphQLType? {
        // If the given type is a list itself.
        if (type is GraphQLList)
            return type.wrappedType

        // If the list is wrapped with a non-null.
        if (type is GraphQLNonNull && type.wrappedType is GraphQLList)
            return (type.wrappedType as GraphQLList).wrappedType

        return null
    }
}
