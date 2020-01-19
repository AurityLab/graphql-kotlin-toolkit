package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
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
     * Will wrap the given [kotlinType] with the same wrapping of the given [type].
     */
    fun wrapType(type: GraphQLType, kotlinType: TypeName): TypeName =
        internalWrapType(type, null, kotlinType)

    /**
     * Will wrap the given [kotlinType] with the same wrapping of the given [type].
     * This method can be supplied with the [parentType] to define the nullability.
     */
    private fun internalWrapType(type: GraphQLType, parentType: GraphQLType?, kotlinType: TypeName): TypeName {
        return when (type) {
            !is GraphQLModifiedType -> {
                // If the unmodified type is reached access the parent and check if it's NoNull.
                return if (parentType is GraphQLNonNull)
                    kotlinType.copy(false)
                else
                    kotlinType.copy(true)
            }
            is GraphQLList -> {
                // Continue to unwrap the wrapped type of this list.
                // When unwrapping is finished then create a parameterized list.
                val list = LIST.parameterizedBy(internalWrapType(type.wrappedType, type, kotlinType))

                // Check if the parent is NonNull.
                if (parentType is GraphQLNonNull)
                    list.copy(false)
                else
                    list.copy(true)
            }
            is GraphQLNonNull -> {
                // If there is a NonNull type just delegate to the wrapped type.
                return internalWrapType(type.wrappedType, type, kotlinType)
            }
            else -> kotlinType
        }
    }
}
