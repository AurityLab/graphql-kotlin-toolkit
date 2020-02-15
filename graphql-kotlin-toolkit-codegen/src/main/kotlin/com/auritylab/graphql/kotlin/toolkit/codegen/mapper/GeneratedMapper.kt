package com.auritylab.graphql.kotlin.toolkit.codegen.mapper

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType

/**
 * Describes a central place in which all components can access the naming for generated classes.
 */
internal class GeneratedMapper(
    private val options: CodegenOptions
) {
    /**
     * Will return the [ClassName] for the given [graphQLType].
     *
     * @throws IllegalArgumentException If no name can be generated for the given [graphQLType]
     */
    fun getGeneratedTypeClassName(graphQLType: GraphQLType, appendCompanion: Boolean = false): ClassName {
        val name = NamingHelper.uppercaseFirstLetter(graphQLType.name)
        return when (graphQLType) {
            is GraphQLEnumType -> buildClassName(name, appendCompanion, "enum")
            is GraphQLInputObjectType -> buildClassName(name, appendCompanion, "inputObject")
            is GraphQLObjectType -> buildClassName(name, appendCompanion, "object")
            else -> throw IllegalArgumentException("Unable to build name for ${graphQLType.name}")
        }
    }

    /**
     * Will return the [ClassName] for the given [field] in the [container].
     */
    fun getGeneratedFieldResolverClassName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): ClassName {
        // Uppercase all parts of the name.
        val containerName = NamingHelper.uppercaseFirstLetter(container.name)
        val fieldName = NamingHelper.uppercaseFirstLetter(field.name)

        return buildClassName(containerName + fieldName, false, "resolver")
    }

    /**
     * Will return the [ClassName] for the value wrapper.
     */
    fun getValueWrapperName(): ClassName =
        buildClassName("V", false, "util")

    /**
     * Will return the [ClassName] for the environment wrapper.
     */
    fun getEnvironmentWrapperClassName(): ClassName =
        buildClassName("Env", false, "util")

    /**
     * Will return the [MemberName] of the builder method for the given [inputObject].
     */
    fun getInputObjectBuilderMemberName(inputObject: GraphQLInputObjectType): MemberName =
        MemberName(getGeneratedTypeClassName(inputObject, true), "buildByMap")

    /**
     * Will return the [MemberName] which points to a string which contains the name of the container for the given field resolver.
     */
    fun getFieldResolverContainerMemberName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): MemberName {
        val resolver = getGeneratedFieldResolverClassName(container, field)

        return MemberName(
            ClassName(resolver.packageName, *resolver.simpleNames.toTypedArray(), "Meta"),
            "CONTAINER"
        )
    }

    /**
     * Will return the [MemberName] which points to a string which contains the name of the field for the given field resolver.
     */
    fun getFieldResolverFieldMemberName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): MemberName {
        val resolver = getGeneratedFieldResolverClassName(container, field)

        return MemberName(
            ClassName(resolver.packageName, *resolver.simpleNames.toTypedArray(), "Meta"),
            "FIELD"
        )
    }

    /**
     * Will return the [ClassName] which points to the Environment class for the given resolver.
     */
    fun getFieldResolverEnvironment(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): ClassName {
        val resolver = getGeneratedFieldResolverClassName(container, field)

        return ClassName(resolver.packageName, *resolver.simpleNames.toTypedArray(), "Environment")
    }

    /**
     * Will build a new [ClassName] with the given [className] as identifier for the class.
     * Additional package tags will be joined with dots (.) and appended to the base package.
     */
    private fun buildClassName(
        className: String,
        appendCompanion: Boolean = false,
        vararg additionalPackageTags: String
    ): ClassName {
        val fullPackage = options.generatedBasePackage +
            additionalPackageTags.joinToString(
                separator = ".",
                prefix = if (additionalPackageTags.isNotEmpty()) "." else ""
            )

        val augmented =
            if (options.generatedGlobalPrefix == null) className
            else "${options.generatedGlobalPrefix}$className"

        return if (appendCompanion)
            ClassName(fullPackage, augmented, "Companion")
        else
            ClassName(fullPackage, augmented)
    }
}
