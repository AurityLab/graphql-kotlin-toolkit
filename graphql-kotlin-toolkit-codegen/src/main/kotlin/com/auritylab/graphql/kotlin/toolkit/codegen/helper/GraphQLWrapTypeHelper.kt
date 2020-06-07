package com.auritylab.graphql.kotlin.toolkit.codegen.helper

import com.squareup.kotlinpoet.COLLECTION
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

internal object GraphQLWrapTypeHelper {

    /**
     * Will wrap the given [kotlinType] with the same wrapping of the given [type].
     * If the wrapped type will be used for a output the parameter [isOutput] shall be set to true.
     */
    fun wrapType(type: GraphQLType, kotlinType: TypeName, isOutput: Boolean, listType: ClassName? = null): TypeName =
        internalWrapType(type, null, kotlinType, isOutput, listType)

    /**
     * Will wrap the given [kotlinType] with the same wrapping of the given [type]. This method can be supplied with
     * the [parentType] to define the nullability. An explicit [listType] can be defined through the parameter. As the
     * explicit [listType] is optional it will fallback to the decision logic which relies on the [isOutput] parameter.
     *
     * @param listType The type to use to represent a [GraphQLList]. It is to have exactly one type variable.
     */
    private fun internalWrapType(
        type: GraphQLType,
        parentType: GraphQLType?,
        kotlinType: TypeName,
        isOutput: Boolean,
        listType: ClassName? = null
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

                // If there is an explicit type for the list given use it. If there is no explicit list type
                // given use a collection if the type is used for a output type, a List if not.
                val list = listType?.parameterizedBy(inner)
                    ?: (if (isOutput) COLLECTION else LIST).parameterizedBy(inner)

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
