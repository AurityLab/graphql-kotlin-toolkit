package com.auritylab.gql.kotlin.poet.mapper

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet.helper.KotlinRepresentationHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import graphql.schema.*
import java.math.BigDecimal
import java.math.BigInteger

class KotlinTypeMapper(
        private val options: PoetOptions,
        private val generateClassName: NameMapper,
        private val schema: GraphQLSchema
) {
    fun getKotlinType(type: GraphQLType): TypeName {
        // Unwrap the given type for the actual object/scalar/etc.

        // Check if the unwrapped type is registered in the type definition registry.
        // Check if the TypeDefinition is a scalar.
        val kType = when (val unwrappedType = if (type is GraphQLModifiedType) unwrapTypeFull(type) else type) {
            is GraphQLScalarType -> {
                getScalarKotlinType(unwrappedType)
            }
            is GraphQLInputObjectType -> {
                getGeneratedClassName(unwrappedType)
            }
            is GraphQLEnumType -> {
                getGeneratedClassName(unwrappedType)
            }
            is GraphQLObjectType -> {
                getObjectKotlinType(unwrappedType)
            }
            else -> getDefaultClassName()
        }

        return applyWrapping(type, null, kType)
    }

    /**
     * Will unwrap the given [type] to the actual type. This is necessary because a incoming type can be wrapped in a
     * [ListType] or a [NonNullType].
     */
    private fun unwrapType(type: GraphQLModifiedType): GraphQLType {
        return when (type) {
            is GraphQLList -> type.wrappedType
            is GraphQLNonNull -> type.wrappedType
            else -> type
        }
    }

    /**
     * Will recursively unwrap the given [type] using [unwrapType]. A type can be wrapped multiple types.
     */
    private fun unwrapTypeFull(type: GraphQLModifiedType): GraphQLType {
        var t: GraphQLType = type
        // Will unwrap until the Type is TypeName.ø
        while (t is GraphQLModifiedType)
            t = unwrapType(t)

        return t
    }

    private fun applyWrapping(thisType: GraphQLType, parentType: GraphQLType?, kType: TypeName): TypeName {
        return when (thisType) {
            !is GraphQLModifiedType -> {
                return if (parentType is GraphQLNonNull)
                    kType.copy(false)
                else
                    kType.copy(true)
            }
            is GraphQLList -> {
                val list = ClassName("kotlin.collections", "List")
                val parameterizedList = list.parameterizedBy(applyWrapping(thisType.wrappedType, thisType, kType))

                // Check if the inner type is already a TypeName.
                val res = applyWrapping(thisType.wrappedType, thisType, parameterizedList)

                if (parentType is GraphQLNonNull)
                    res.copy(false)
                else
                    res.copy(true)
            }
            is GraphQLNonNull -> {
                return applyWrapping(thisType.wrappedType, thisType, kType)
            }
            else -> kType
        }
    }

    /**
     * Will take the given [scalarTypeDefinition] and check if it's a default scalar or a custom one.
     */
    private fun getScalarKotlinType(scalarTypeDefinition: GraphQLScalarType): ClassName {
        // Check for the default scalars of GraphQL itself and the `graphql-java` library.
        val defaultClass = when (scalarTypeDefinition.name) {
            "String" -> String::class
            "Boolean" -> Boolean::class
            "Int" -> Int::class
            "Float" -> Float::class
            "ID" -> String::class
            "Long" -> Long::class
            "Short" -> Short::class
            "Byte" -> Byte::class
            "BigDecimal" -> BigDecimal::class
            "BigInteger" -> BigInteger::class
            else -> null
        }

        // If it's a default scalar just return its ClassName.
        if (defaultClass != null)
            return defaultClass.asClassName()

        // Fetch the kotlin representation class or return "Any".
        return KotlinRepresentationHelper.getClassName(scalarTypeDefinition)
                ?: ClassName("kotlin", "Any")
    }

    private fun getObjectKotlinType(type: GraphQLObjectType): ClassName {
        return KotlinRepresentationHelper.getClassName(type)
                ?: getDefaultClassName()
    }

    /**
     * Will fetch the [ClassName] for the generated class of the given [typeDefinition].
     */
    private fun getGeneratedClassName(typeDefinition: GraphQLType): ClassName {
        val fullName = generateClassName.getTypeName(typeDefinition)
                ?: return getDefaultClassName()

        return ClassName(fullName.packageName, fullName.className)
    }

    /**
     * Will return the default [ClassName] which is used as a fallback. Defaults to [Any].
     */
    private fun getDefaultClassName(): ClassName = ClassName("kotlin", "Any")
}
