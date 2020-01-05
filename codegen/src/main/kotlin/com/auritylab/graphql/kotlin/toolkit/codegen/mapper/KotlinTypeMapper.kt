package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLTypeHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.KotlinRepresentationHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLModifiedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import java.math.BigDecimal
import java.math.BigInteger

internal class KotlinTypeMapper(
    private val options: CodegenInternalOptions,
    private val generatedMapper: GeneratedMapper
) {
    fun getKotlinType(type: GraphQLType): TypeName {
        // Unwrap the given type for the actual object/scalar/etc.

        // Check if the unwrapped type is registered in the type definition registry.
        // Check if the TypeDefinition is a scalar.
        val kType = when (val unwrappedType =
            if (type is GraphQLModifiedType) GraphQLTypeHelper.unwrapTypeFull(type) else type) {
            is GraphQLScalarType -> {
                getScalarKotlinType(unwrappedType)
            }
            is GraphQLInputObjectType -> {
                generatedMapper.getGeneratedTypeClassName(unwrappedType)
            }
            is GraphQLEnumType -> {
                generatedMapper.getGeneratedTypeClassName(unwrappedType)
            }
            is GraphQLObjectType -> {
                getObjectKotlinType(unwrappedType)
            }
            else -> getDefaultClassName()
        }

        return applyWrapping(type, null, kType)
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
     * Will return the default [ClassName] which is used as a fallback. Defaults to [Any].
     */
    private fun getDefaultClassName(): ClassName = ClassName("kotlin", "Any")
}
