package com.auritylab.graphql.kotlin.toolkit.common.helper

import graphql.com.google.common.base.Objects
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

object GraphQLEqualityHelper {
    /**
     * Checks if the given [GraphQLType]s are equal. The [GraphQLModifiedType]s are also considered.
     */
    fun isEqual(first: GraphQLType, second: GraphQLType): Boolean {
        val firstUnwrapped = GraphQLTypeHelper.unwrapTypeLayers(first)
        val secondUnwrapped = GraphQLTypeHelper.unwrapTypeLayers(second)

        // If the sizes of the lists do not match, then return false.
        if (firstUnwrapped.size != secondUnwrapped.size) {
            return false
        }

        firstUnwrapped.forEachIndexed { index, type ->
            if (type !is GraphQLModifiedType && !Objects.equal(type, secondUnwrapped[index])) {
                // If it's not a modified type and the types in both lists do not match, then return false
                return false
            }

            // If it's a non null type and the types do not match, then return false.
            if (type is GraphQLNonNull && !type.isEqualTo(secondUnwrapped[index])) {
                return false
            }

            // If it's a list type and the types do not match, then return false
            if (type is GraphQLList && !type.isEqualTo(secondUnwrapped[index])) {
                return false
            }
        }

        return true
    }
}
