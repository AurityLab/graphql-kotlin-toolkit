package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.codeblock.ArgumentCodeBlockGenerator
import com.auritylab.graphql.kotlin.toolkit.codegen.helper.NamingHelper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLInputObjectType

/**
 * Implements a [AbstractGenerator] which will generate the source code for a [GraphQLInputObjectType].
 * It will generate the actual `data class` and a method which can parse a map to the `data class`
 */
internal class InputObjectGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper,
    private val argumentCodeBlockGenerator: ArgumentCodeBlockGenerator
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    fun getInputObject(inputObject: GraphQLInputObjectType): FileSpec {
        val fieldResolverName = generatedMapper.getGeneratedTypeClassName(inputObject)

        return getFileSpecBuilder(fieldResolverName)
            .addType(buildInputObjectTypeClass(inputObject))
            .build()
    }

    /**
     * Will create the [TypeSpec] which represents the `data class` for the given [inputObject].
     */
    private fun buildInputObjectTypeClass(inputObject: GraphQLInputObjectType): TypeSpec {
        return TypeSpec.classBuilder(getGeneratedTypeClassName(inputObject))
            // Add the `DATA` modifier to make it a `data class`.
            .addModifiers(KModifier.DATA)
            // Create the primary constructor with all available parameters.
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(buildParameters(inputObject))
                    .build()
            )
            .addProperties(buildProperties(inputObject))
            .addType(buildInputObjectTypeCompanionObject(inputObject))
            .build()
    }

    private fun buildInputObjectTypeCompanionObject(inputObject: GraphQLInputObjectType): TypeSpec {
        val type = TypeSpec.companionObjectBuilder()

        inputObject.fields.forEach {
            type.addFunction(argumentCodeBlockGenerator.buildArgumentResolverFun(it.name, it.type, it))
        }

        type.addFunction(createBuilderFun(inputObject))

        return type.build()
    }

    /**
     * Will create a [ParameterSpec] for each field in the given [inputObject] to use in the primary constructor.
     */
    private fun buildParameters(inputObject: GraphQLInputObjectType): Collection<ParameterSpec> {
        return inputObject.fields.map { field ->
            val kType = getKotlinType(field.type, field)

            ParameterSpec(field.name, kType)
        }
    }

    /**
     * Will create a [PropertySpec] for each field in the given [inputObject] to use in the `data class`.
     * Each property will be initialized by the corresponding parameter in the primary constructor.
     */
    private fun buildProperties(inputObject: GraphQLInputObjectType): Collection<PropertySpec> {
        return inputObject.fields.map { field ->
            val kType = getKotlinType(field.type, field)

            PropertySpec.builder(field.name, kType)
                .initializer(field.name)
                .build()
        }
    }

    private fun createBuilderFun(inputObject: GraphQLInputObjectType): FunSpec {
        val builderMemberName = generatedMapper.getInputObjectBuilderMemberName(inputObject)
        val inputObjectClassName = generatedMapper.getGeneratedTypeClassName(inputObject)

        return FunSpec.builder(builderMemberName.simpleName)
            .addParameter("map", MAP.parameterizedBy(STRING, ANY))
            .returns(inputObjectClassName)
            .also { spec ->
                val namedParameters = inputObject.fields.joinToString(", ") {
                    "${it.name} = resolve${NamingHelper.uppercaseFirstLetter(it.name)}(map)"
                }

                spec.addStatement("return %T($namedParameters)", inputObjectClassName)
            }
            .build()
    }
}
