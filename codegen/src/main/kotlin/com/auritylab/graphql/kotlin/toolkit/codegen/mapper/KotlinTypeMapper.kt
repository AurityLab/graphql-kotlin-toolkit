package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.DirectiveHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import java.math.BigDecimal
import java.math.BigInteger

internal class KotlinTypeMapper(
    private val options: CodegenOptions,
    private val generatedMapper: GeneratedMapper
) {
    /**
     * Will try to find a corresponding [TypeName] for the given [type].
     * The returned types already assume the incoming data has been parsed.
     */
    fun getKotlinType(type: GraphQLType): TypeName {
        val res = when (val unwrappedType = GraphQLTypeHelper.unwrapType(type)) {
            is GraphQLScalarType -> getScalarKotlinType(unwrappedType)
            is GraphQLInputObjectType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLEnumType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLObjectType -> getObjectKotlinType(unwrappedType)
            else -> ANY
        }

        // Apply the wrapping of the GraphQL type to the Kotlin type.
        return GraphQLTypeHelper.wrapType(type, res)
    }

    /**
     * Will Try to find a corresponding [TypeName] for the given [type].
     * Thr returned types represent the incoming types from graphql-java.
     */
    fun getInputKotlinType(type: GraphQLType): TypeName {
        val res = when (val unwrappedType = GraphQLTypeHelper.unwrapType(type)) {
            is GraphQLScalarType -> getScalarKotlinType(unwrappedType)
            is GraphQLInputObjectType -> MAP.parameterizedBy(STRING, ANY)
            is GraphQLEnumType -> STRING
            is GraphQLObjectType -> getObjectKotlinType(unwrappedType)
            else -> ANY
        }

        // Apply the wrapping of the GraphQL type to the Kotlin type.
        return GraphQLTypeHelper.wrapType(type, res)
    }

    /**
     * Will build a [MemberName] which points to the builder function of the given [inputObject].
     */
    fun getInputObjectBuilder(inputObject: GraphQLInputObjectType): MemberName {
        return generatedMapper.getInputObjectBuilderMemberName(inputObject)
    }

    /**
     * Will take the given [scalarTypeDefinition] and check if it's a default scalar or a custom one.
     */
    private fun getScalarKotlinType(scalarTypeDefinition: GraphQLScalarType): ClassName {
        // Check for the default scalars of GraphQL itself and the `graphql-java` library.
        val defaultClass = when (scalarTypeDefinition.name) {
            "String" -> STRING
            "Boolean" -> BOOLEAN
            "Int" -> INT
            "Float" -> FLOAT
            "ID" -> STRING
            "Long" -> LONG
            "Short" -> SHORT
            "Byte" -> BYTE
            "BigDecimal" -> BigDecimal::class.asClassName()
            "BigInteger" -> BigInteger::class.asClassName()
            else -> null
        }

        // If it's a default scalar just return its ClassName.
        if (defaultClass != null) return defaultClass

        // Fetch the kotlin representation class or return "Any".
        return DirectiveHelper.getRepresentationClass(scalarTypeDefinition) ?: ANY
    }

    /**
     * Will return the [ClassName] for the given [type].
     */
    private fun getObjectKotlinType(type: GraphQLObjectType): ClassName {
        // Check if the type is annotated with the "kotlinRepresentation" directive.
        val helperResult = DirectiveHelper.getRepresentationClass(type)

        // If the directive is available return the content.
        if (helperResult != null)
            return helperResult

        return if (options.generateAll || DirectiveHelper.hasGenerateDirective(type)) {
            // The object has a generated type, return the generated one.
            generatedMapper.getGeneratedTypeClassName(type, false)
        } else
        // The object does not have a generated type, therefore return ANY.
            ANY
    }
}
