package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.GraphQLWrapTypeHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.toReadableName
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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STAR
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
    private val generatedMapper: GeneratedMapper,
    private val bindingMapper: BindingMapper,
) {
    /**
     * Will resolve the [TypeName] for the given [GraphQLType]. This will always return a [TypeName], but in the worst
     * case you'll get [ANY]. If type information for a specific type is available, depends on the schema.
     *
     * The following subtypes of [GraphQLType] are supported:
     * * [GraphQLScalarType]: Built-in scalars can be resolved automatically, but custom scalars need
     *   a "@kRepresentation" directive.
     * * [GraphQLInputObjectType]: Provides the type which will be generated for it.
     * * [GraphQLEnumType]: Provides the type which will be generated for it.
     * * [GraphQLObjectType]: Provides the matching "@kPresentation" type or the generated type. (Generated type may
     *   not be available).
     * * [GraphQLInterfaceType]: Provides the matching "@kPresentation" type or [ANY] if there's none.
     * * [GraphQLUnionType]: If all types of the union are of the same Kotlin type, then the according type will be
     *   returned, otherwise [ANY] will be returned.
     *
     * You can additionally supply a [fieldDirectiveContainer] which contains directives relevant for type generation.
     * The only use-case at the moment is to detect the kDoubleNull directive to internally wrap the type with
     *  the value-wrapper.
     *
     * A custom [listType] can also be supplied if needed. By default, a [Collection] will be used for GraphQL lists.
     * Attention: The given [ClassName] will be parameterized with the inner type. Therefore make sure the type
     * defines exactly one type parameter.
     *
     * As a type can also be parameterized, there are two options to define define the behavior.
     * * If a type is parameterized, then you can set [withParameters] to false, to explicitly remove them.
     * * If a type is parameterized, then you can set [withStarProjectionsOnly] to replace all given parameters with
     *   star-projects. This is useful in some cases where you cant provide type information.
     *
     * @param type The type for which to create the [TypeName] for.
     * @param fieldDirectiveContainer The directive container for additional specification of the type.
     * @param listType A custom type for lists. Type must have exactly one parameter.
     * @param withParameters If type parameters shall be included. By default true.
     * @param withStarProjectionsOnly If all available parameters shall be replaced with star-projections.
     * @return The resolved [TypeName] for the given [type].
     */
    fun getKotlinType(
        type: GraphQLType,
        fieldDirectiveContainer: GraphQLDirectiveContainer? = null,
        listType: ClassName? = null,
        withParameters: Boolean = true,
        withStarProjectionsOnly: Boolean = false
    ): TypeName {
        var res = when (val unwrappedType = GraphQLTypeHelper.unwrapType(type)) {
            is GraphQLScalarType -> getScalarKotlinType(unwrappedType)
            is GraphQLInputObjectType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLEnumType -> generatedMapper.getGeneratedTypeClassName(unwrappedType)
            is GraphQLObjectType -> getObjectKotlinType(unwrappedType)
            is GraphQLInterfaceType -> getInterfaceKotlinType(unwrappedType)
            is GraphQLUnionType -> getUnionKotlinType(unwrappedType)
            else -> ANY
        }

        // If the caller explicitly DOES NOT want parameterized types, then just remove them.
        if (!withParameters && res is ParameterizedTypeName) {
            res = res.rawType
        }

        // If parameters shall be included, but each parameter shall be converted to a star-projection, then
        // just take all parameters and map them to star-projections.
        if (withParameters && withStarProjectionsOnly && res is ParameterizedTypeName) {
            res = replaceParametersWithStarProjections(res)
        }

        // Apply the wrapping of the GraphQL type to the Kotlin type.
        val wrapped = GraphQLWrapTypeHelper.wrapType(type, res, true, listType)

        // If there is a directive container and contains the DoubleNull directive, the type will additionally
        // be wrapped into a ValueWrapper.
        return if (fieldDirectiveContainer != null &&
            DirectiveFacade.Defaults.doubleNull[fieldDirectiveContainer] &&
            wrapped.isNullable
        )
            bindingMapper.valueType.parameterizedBy(wrapped).copy(true)
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
        return DirectiveFacade.Defaults.representation.getArguments(scalar)
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
    private fun getObjectKotlinType(type: GraphQLObjectType): TypeName {
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
    private fun getInterfaceKotlinType(type: GraphQLInterfaceType): TypeName =
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
    private fun resolveRepresentationClass(container: GraphQLDirectiveContainer): TypeName? {
        // Check if the representation directive is present on the container.
        val arguments = DirectiveFacade.Defaults.representation.getArguments(container)
            ?: return null

        val className = arguments.className
        val parameters = arguments.parameters

        // If no class argument is set, just return null
        if (className.isNullOrBlank())
            return null

        // Try to take a guess on the class name.
        val resolvedClassName = try {
            ClassName.bestGuess(className)
        } catch (ex: IllegalArgumentException) {
            // Wrap the exception of "bestGuess" with an more detailed Exception.
            val message = String.format("Invalid class (%s) has been set on %s", className, container.toReadableName())
            throw IllegalArgumentException(message, ex)
        }

        // If there are no parameters set, then just return the resolved ClassName.
        if (parameters.isNullOrEmpty())
            return resolvedClassName

        // Build the parameterized types and parameterize the resolved type.
        return resolvedClassName.parameterizedBy(buildParameters(parameters, container))
    }

    /**
     * Will resolve the [ClassName] for the given [type]. This will check if the [CodegenOptions.generateAll] attribute
     * is true or the type is annotated with the [DirectiveFacade.generate] directive.
     */
    private fun resolveGeneratedClass(type: GraphQLNamedType): ClassName =
        if (options.generateAll || (type is GraphQLDirectiveContainer && DirectiveFacade.Defaults.generate[type]))
            generatedMapper.getGeneratedTypeClassName(type)
        else ANY

    /**
     * Will build a [List] of type parameters based on the given [raw] input. The [raw] input consists of FQN names
     * of classes (Or literal stars for a star-projection).
     * This might throw an exception if the raw input can't resolve to an actual class or is simply and invalid value.
     *
     * @param raw List of raw parameters. Consists of FQN classes or literal stars for star-projections.
     * @param origin Directive container which contains the kRepresentation directive (required for better error messages).
     * @return The [List] of resolved [TypeName] by the [raw] list. The order is the same as the [raw] List.
     */
    private fun buildParameters(raw: List<String>, origin: GraphQLDirectiveContainer): List<TypeName> {
        return raw.mapIndexed { index, it ->
            when (it) {
                // Map a literal star to the star-projection
                "*" -> STAR
                else -> {
                    // Try to guess the class name by the given string.
                    // If it fails, an according exception will be thrown.
                    try {
                        ClassName.bestGuess(it)
                    } catch (ex: IllegalArgumentException) {
                        throw IllegalArgumentException(
                            String.format(
                                "Invalid class (%s) has been set as parameter on index %d on %s",
                                it,
                                index,
                                origin.toReadableName()
                            ), ex
                        )
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Will replace all parameters defined in the given [ParameterizedTypeName] with star-projections ([STAR]).
         */
        fun replaceParametersWithStarProjections(type: ParameterizedTypeName): TypeName =
            type.rawType.parameterizedBy(type.typeArguments.map { STAR })
    }
}
