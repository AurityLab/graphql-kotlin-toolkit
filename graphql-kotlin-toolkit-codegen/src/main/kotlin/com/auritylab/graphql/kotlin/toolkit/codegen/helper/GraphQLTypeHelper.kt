package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
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
     * If the wrapped type will be used for a output the parameter [isOutput] shall be set to true.
     */
    fun wrapType(type: GraphQLType, kotlinType: TypeName, isOutput: Boolean): TypeName =
        internalWrapType(type, null, kotlinType, isOutput)

    /**
     * Will wrap the given [kotlinType] with the same wrapping of the given [type].
     * This method can be supplied with the [parentType] to define the nullability.
     */
    private fun internalWrapType(
        type: GraphQLType,
        parentType: GraphQLType?,
        kotlinType: TypeName,
        isOutput: Boolean
    ): TypeName {
        return when (type) {
            !is GraphQLModifiedType -> {
                // Per default all types are nullable in GraphQL,
                // therefore always return nullable types for top level types.
                if (parentType == null)
                    return kotlinType.copy(true)

                // If the unmodified type is reached access the parent and check if it's NoNull.
                return if (parentType is GraphQLNonNull)
                    kotlinType.copy(false)
                else
                    kotlinType.copy(true)
            }
            is GraphQLList -> {
                // Create the wrapped type of the wrapped type of the list.
                val inner = internalWrapType(type.wrappedType, type, kotlinType, isOutput)

                // Use a Collection if the type is used for a output type, a List of not.
                val list = (if (isOutput) COLLECTION else LIST).parameterizedBy(inner)

                // Check if the parent is NonNull.
                if (parentType is GraphQLNonNull)
                    list.copy(false)
                else
                    list.copy(true)
            }
            is GraphQLNonNull -> {
                // If there is a NonNull type just delegate to the wrapped type.
                return internalWrapType(type.wrappedType, type, kotlinType, isOutput)
            }
            else -> kotlinType
        }
    }
}
