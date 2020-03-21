package com.auritylab.graphql.kotlin.toolkit.common.helper

import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

object GraphQLTypeHelper {
    /**
     * Will unwrap the given [type] until the type is no longer instance of [GraphQLModifiedType].
     * This will just return the non [GraphQLModifiedType] aka the most inner type.
     *
     * @param type The type to unwrap.
     * @return The most inner type of the given type.
     */
    fun unwrapType(type: GraphQLType): GraphQLType =
        unwrapTypeLayers(type).last()

    /**
     * Will unwrap the given [type] until the type is no longer instance of [GraphQLModifiedType]. Each layer of the
     * unwrapping process will be returned in the [List]. The list is sorted accordingly to the unwrapping. The final
     * non [GraphQLModifiedType] will also be added to the last on the last index.
     *
     * @param type The type to unwrap.
     * @return List of all [GraphQLType].
     */
    fun unwrapTypeLayers(type: GraphQLType): List<GraphQLType> {
        val wraps = mutableListOf<GraphQLType>()

        // Start with the given type.
        var c = type

        // Iterate until there is no longer a modified type.
        while (c is GraphQLModifiedType) {
            // Add the current type to the layers.
            wraps.add(c)
            // Set the current type to the wrapped type.
            c = c.wrappedType
        }

        // Add the non modified type to the list.
        wraps.add(c)

        return wraps
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
