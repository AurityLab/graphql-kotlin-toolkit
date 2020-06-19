package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLWrapTypeHelper
import com.auritylab.graphql.kotlin.toolkit.common.directive.DirectiveFacade
import com.auritylab.graphql.kotlin.toolkit.common.helper.GraphQLTypeHelper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
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
import graphql.schema.GraphQLNamedType
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
     * Will resolve the [TypeName] for the given [GraphQLType]. The returned types already assume the incoming data
     * has been parsed into internal types. You can additionally supply a [fieldDirectiveContainer] which can be for
     * example the [graphql.schema.GraphQLFieldDefinition]. A custom [listType] can also be supplied if needed.
     *
     * @param type The type for which to create the [TypeName] for.
     * @param fieldDirectiveContainer The directive container for additional specification of the type.
     * @param listType A custom type for lists.
     * @return The resolved [TypeName] for the given [type].
     */
    fun getKotlinType(
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer? = null,
        listType: ClassName? = null
    ): TypeName {
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
        val wrapped = GraphQLWrapTypeHelper.wrapType(type, res, true, listType)

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
    fun getInputKotlinType(type: GraphQLType, listType: ClassName? = null): TypeName {
        val res = when (val unwrappedType = GraphQLTypeHelper.unwrapType(type)) {
            is GraphQLScalarType -> getScalarKotlinType(unwrappedType)
            is GraphQLInputObjectType -> MAP.parameterizedBy(STRING, ANY)
            is GraphQLEnumType -> STRING
            is GraphQLObjectType -> getObjectKotlinType(unwrappedType)
            else -> ANY
        }

        // Apply the wrapping of the GraphQL type to the Kotlin type.
        return GraphQLWrapTypeHelper.wrapType(type, res, false, listType)
    }

    /**
     * Will build a [MemberName] which points to the builder function of the given [inputObject].
     */
    fun getInputObjectBuilder(inputObject: GraphQLInputObjectType): MemberName {
        return generatedMapper.getInputObjectBuilderMemberName(inputObject)
    }

    /**
     * Will return the according [ClassName] of the given [scalar]. This will always prefer representation classes
     * over the built-in representation. If there is no representation and it's no built-in scalar, it will
     * simply return Any.
     *
     * @param scalar The scalar for which to get the [ClassName] for.
     * @return The [ClassName] for the given [scalar].
     */
    private fun getScalarKotlinType(scalar: GraphQLScalarType): ClassName {
        // Load the default class if the given scalar is a built-in scalar.
        val defaultClass = getBuiltInScalarKotlinType(scalar)

        // Return the representation of the scalar or the default class for built-in scalars or any.
        return DirectiveFacade.representation.getArguments(scalar)
            ?.className?.let { ClassName.bestGuess(it) }
            ?: defaultClass
            ?: ANY
    }

    /**
     * Will return the according [ClassName] for the given [scalar]. This will take care about the built-in
     * default scalars.
     *
     * @see getScalarKotlinType
     * @param scalar The scalars for which to get the [ClassName] for.
     * @return The [ClassName] for the given [scalar].
     */
    private fun getBuiltInScalarKotlinType(scalar: GraphQLScalarType): ClassName? {
        return when (scalar.name) {
            "String" -> STRING
            "Boolean" -> BOOLEAN
            "Int" -> INT
            "Float" -> DOUBLE
            "ID" -> STRING
            "Long" -> LONG
            "Short" -> SHORT
            "Byte" -> BYTE
            "Char" -> CHAR
            "BigDecimal" -> BigDecimal::class.asClassName()
            "BigInteger" -> BigInteger::class.asClassName()
            else -> null
        }
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
        DirectiveFacade.representation.getArguments(container)?.className?.let { ClassName.bestGuess(it) }

    /**
     * Will resolve the [ClassName] for the given [type]. This will check if the [CodegenOptions.generateAll] attribute
     * is true or the type is annotated with the [DirectiveFacade.generate] directive.
     */
    private fun resolveGeneratedClass(type: GraphQLNamedType): ClassName =
        if (options.generateAll || (type is GraphQLDirectiveContainer && DirectiveFacade.generate[type]))
            generatedMapper.getGeneratedTypeClassName(type)
        else ANY
}
