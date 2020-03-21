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
     * Will build the generated [ClassName] for the given [GraphQLType]. This will only build [ClassName]s which
     * are generated through this code generator.
     *
     * @throws IllegalArgumentException If no name can be generated for the given [graphQLType]
     */
    fun getGeneratedTypeClassName(graphQLType: GraphQLType): ClassName {
        val name = NamingHelper.uppercaseFirstLetter(graphQLType.name)
        return when (graphQLType) {
            is GraphQLEnumType -> buildClassName(name, "enum")
            is GraphQLInputObjectType -> buildClassName(name, "inputObject")
            is GraphQLObjectType -> buildClassName(name, "object")
            else -> throw IllegalArgumentException("Unable to build name for ${graphQLType.name}")
        }
    }

    /**
     * Will return the [ClassName] for a resolver which is defined through the given [field], which is within the
     * given [container]. The [container] is necessary to build a unique identifier for the resolver.
     */
    fun getGeneratedFieldResolverClassName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): ClassName {
        // Uppercase all parts of the name.
        val containerName = NamingHelper.uppercaseFirstLetter(container.name)
        val fieldName = NamingHelper.uppercaseFirstLetter(field.name)

        return buildClassName("$containerName$fieldName", "resolver")
    }

    /**
     * Will return the [ClassName] for the value wrapper.
     */
    fun getValueWrapperName(): ClassName =
        buildClassName("V", "util")

    /**
     * Will return the [ClassName] for the environment wrapper.
     */
    fun getEnvironmentWrapperClassName(): ClassName =
        buildClassName("Env", "util")

    /**
     * Will return the [MemberName] of the builder method for the given [inputObject].
     */
    fun getInputObjectBuilderMemberName(inputObject: GraphQLInputObjectType): MemberName =
        MemberName(getGeneratedTypeClassName(inputObject).addSimpleNames("Companion"), "buildByMap")

    /**
     * Will return the [MemberName] which points to a string which contains the name of the container for the given field resolver.
     */
    fun getFieldResolverContainerMemberName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): MemberName =
        MemberName(getGeneratedFieldResolverClassName(container, field).addSimpleNames("Meta"), "CONTAINER")

    /**
     * Will return the [MemberName] which points to a string which contains the name of the field for the given field resolver.
     */
    fun getFieldResolverFieldMemberName(
        container: GraphQLFieldsContainer,
        field: GraphQLFieldDefinition
    ): MemberName =
        MemberName(getGeneratedFieldResolverClassName(container, field).addSimpleNames("Meta"), "FIELD")

    /**
     * Will return the [ClassName] which points to the Environment class for the given resolver.
     */
    fun getFieldResolverEnvironment(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): ClassName =
        getGeneratedFieldResolverClassName(container, field).addSimpleNames("Env")

    fun getPaginationInfoClassName(): ClassName {
        return buildClassName("PaginationInfo", "pagination")
    }

    fun getPaginationInfoBuilderMemberName(): MemberName =
        MemberName(getPaginationInfoClassName().addSimpleNames("Companion"), "buildByMap")

    fun getPaginationConnectionClassName(): ClassName {
        return buildClassName("PaginationConnection", "pagination")
    }

    fun getPaginationEdgeClassName(): ClassName {
        return buildClassName("PaginationEdge", "pagination")
    }

    fun getPaginationPageInfoClassName(): ClassName {
        return buildClassName("PaginationPageInfo", "pagination")
    }

    /**
     * Will build a new [ClassName] with the given [className] as identifier for the class.
     * Additional package tags will be joined with dots (.) and appended to the base package.
     */
    private fun buildClassName(
        className: String,
        additionalPackage: String = ""
    ): ClassName {
        // Create the package for the Classname.
        val fullPackage = options.generatedBasePackage + "." + additionalPackage

        // Augment the name of the class with the global prefix.
        val augmented =
            if (options.generatedGlobalPrefix == null) className
            else "${options.generatedGlobalPrefix}$className"

        // Build the ClassName.
        return ClassName(fullPackage, augmented)
    }

    /**
     * Will create a copy of the [ClassName] and add the given [name]s as [ClassName.simpleNames].
     *
     * @param name The names to add as simple names to the ClassName.
     * @return The new [ClassName] with the added simple names.
     */
    private fun ClassName.addSimpleNames(vararg name: String): ClassName {
        return ClassName(this.packageName, *this.simpleNames.toTypedArray(), *name)
    }
}
