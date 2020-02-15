package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.schema.GraphQLEnumType

/**
 * Implements a [AbstractClassGenerator] which will generate the source code for a [GraphQLEnumType].
 * This will generate the actual `enum` which an additional [String] value.
 */
internal class EnumGenerator(
    private val enumType: GraphQLEnumType,
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    generatedMapper: GeneratedMapper
) : AbstractClassGenerator(options, kotlinTypeMapper, generatedMapper) {
    override val fileClassName: ClassName = getGeneratedType(enumType)

    override fun build(builder: FileSpec.Builder) {
        builder.addType(buildEnumClass(enumType))
    }

    private fun buildEnumClass(enum: GraphQLEnumType): TypeSpec {
        return TypeSpec.enumBuilder(getGeneratedType(enum))
            // Create the primary constructor with a "stringValue" parameter.
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("stringValue", String::class)
                    .build()
            )
            // Create the "stringValue" property which will be initialized by the previously created primary constructor.
            .addProperty(
                PropertySpec.builder("stringValue", String::class)
                    .initializer("stringValue")
                    .build()
            )
            .also {
                // Go through all enum values and create enum constants within this enum.
                enum.values.forEach { enum ->
                    it.addEnumConstant(
                        enum.name, TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter("%S", enum.name)
                            .build()
                    )
                }
            }
            .build()
    }
}
