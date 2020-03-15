package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import graphql.schema.GraphQLDirectiveContainer
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInterfaceType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLUnionType
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Describes a central place which is capable of converting given [GraphQLType]s to Kotlin types.
 */
internal class KotlinTypeMapper(
    private val options: CodegenOptions,
    private val generatedMapper: GeneratedMapper
) {
    /**
     * Will try to find a corresponding [TypeName] for the given [type].
     * The returned types already assume the incoming data has been parsed.
     */
    fun getKotlinType(type: GraphQLType, fieldDirectiveContainer: GraphQLDirectiveContainer? = null): TypeName {
        val res = when (val unwrappedType = GraphQLTypeHelper.unwrapType(type)) {
            is GraphQLScalarType -> getScalarKotlinType(unwrappedType)
            is GraphQLInputObjectType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLEnumType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLObjectType -> getObjectKotlinType(unwrappedType)
            is GraphQLInterfaceType -> getInterfaceKotlinType(unwrappedType)
            is GraphQLUnionType -> getUnionKotlinType(unwrappedType)
            else -> ANY
        }

        // Apply the wrapping of the GraphQL type to the Kotlin type.
        val wrapped = GraphQLTypeHelper.wrapType(type, res, true)

        // If there is a directive container and contains the DoubleNull directive, the type will additionally
        // be wrapped into a ValueWrapper.
        return if (fieldDirectiveContainer != null &&
            DirectiveFacade.doubleNull[fieldDirectiveContainer] &&
            wrapped.isNullable
        )
            generatedMapper.getValueWrapperName().parameterizedBy(wrapped).copy(true)
        else
            wrapped
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
        return GraphQLTypeHelper.wrapType(type, res, false)
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
            "Float" -> DOUBLE
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
        return DirectiveFacade.representation.getArguments(scalarTypeDefinition)?.className ?: ANY
    }

    /**
     * Will return the [ClassName] for the given [type].
     */
    private fun getObjectKotlinType(type: GraphQLObjectType): ClassName {
        // Try to resolve the class via the representation directive.
        // Return the representation class if its available.
        resolveRepresentationClass(type)
            ?.let { return it }

        // Try to resolve the type via a generated class. This will fallback to ANY.
        return resolveGeneratedClass(type)
    }

    /**
     * Will return the [ClassName] for the given [GraphQLInterfaceType].
     */
    private fun getInterfaceKotlinType(type: GraphQLInterfaceType): ClassName =
        // Resolve the representation and return if it's available.
        resolveRepresentationClass(type) ?: ANY

    /**
     * Will return the [ClassName] for the given [GraphQLUnionType]. This will basically just check if the types of
     * the union have the same representation. If they do have the same representation the type will be returned.
     * If they to do NOT have the same representation [ANY] will be returned.
     */
    private fun getUnionKotlinType(type: GraphQLUnionType): TypeName {
        val mappedTypes = type.types
            .map { getKotlinType(it, if (it is GraphQLDirectiveContainer) it else null) }

        val firstType = mappedTypes.first()
        return if (mappedTypes.all { it == firstType }) firstType else ANY
    }

    /**
     * Will resolve the [ClassName] for the given [container]. This will basically just check if the
     * [DirectiveFacade.representation] directive is given, if so it will return the value of the className argument.
     */
    private fun resolveRepresentationClass(container: GraphQLDirectiveContainer): ClassName? =
        // Check if the type is annotated with the "kRepresentation" directive.
        DirectiveFacade.representation.getArguments(container)?.className

    /**
     * Will resolve the [ClassName] for the given [type]. This will check if the [CodegenOptions.generateAll] attribute
     * is true or the type is annotated with the [DirectiveFacade.generate] directive.
     */
    private fun resolveGeneratedClass(type: GraphQLType): ClassName =
        if (options.generateAll || (type is GraphQLDirectiveContainer && DirectiveFacade.generate[type]))
            generatedMapper.getGeneratedTypeClassName(type, false)
        else ANY
}
