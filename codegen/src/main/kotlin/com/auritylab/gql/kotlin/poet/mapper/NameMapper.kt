package com.auritylab.gql.kotlin.poet.mapper

import com.auritylab.gql.kotlin.poet.PoetOptions
import com.auritylab.gql.kotlin.poet.helper.NamingHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import graphql.schema.*

/**
 * Describes a central place in which all components can access the naming for generated classes.
 */
class NameMapper(
        private val options: PoetOptions
) {
    /**
     * Will return the [FullName] for the given [typeDefinition] if a naming is available.
     *
     * @throws IllegalArgumentException If no name can be generated for the given [typeDefinition]
     */
    fun getTypeName(typeDefinition: GraphQLType): FullName {
        val typeName = typeDefinition.name
        when (typeDefinition) {
            is GraphQLEnumType -> {
                val augmentedEnumName = NamingHelper.uppercaseFirstLetter(typeName)

                return if (options.generatedEnumPrefix == null)
                    buildFullName(augmentedEnumName)
                else
                    buildFullName(options.generatedEnumPrefix + augmentedEnumName)
            }
            is GraphQLInputObjectType -> {
                val augmentedInputObjectName = NamingHelper.uppercaseFirstLetter(typeName)

                return if (options.generatedInputObjectPrefix == null)
                    buildFullName(augmentedInputObjectName)
                else
                    buildFullName(options.generatedInputObjectPrefix + augmentedInputObjectName)
            }
            else -> {
                throw IllegalArgumentException("Unable to build name for ${typeDefinition.name}")
            }
        }
    }

    /**
     * Will return the [FullName] for the given [field] in the given [container] if a naming is available.
     */
    fun getFieldResolverName(container: GraphQLFieldsContainer, field: GraphQLFieldDefinition): FullName {
        val containerName = NamingHelper.uppercaseFirstLetter(container.name)
        val fieldName = NamingHelper.uppercaseFirstLetter(field.name)

        val base = containerName + fieldName

        return if (options.generatedResolverPrefix == null)
            buildFullName(base)
        else
            buildFullName(options.generatedResolverPrefix + base)
    }

    fun getValueWrapperName(): FullName {
        return buildFullName("V")
    }

    fun getInputObjectParser(inputObject: GraphQLInputObjectType): MemberName {
        return MemberName(
                ClassName(options.generatedFilesPackage, "InputObjectParsers"),
                "build" + NamingHelper.uppercaseFirstLetter(inputObject.name))
    }

    /**
     * Will build a new [FullName] with the given [className].
     */
    private fun buildFullName(className: String): FullName =
            if (options.generatedFilesPrefix == null)
                FullName(options.generatedFilesPackage, className)
            else
                FullName(options.generatedFilesPackage, options.generatedFilesPrefix + className)


    data class FullName(val packageName: String, val className: String)
}
