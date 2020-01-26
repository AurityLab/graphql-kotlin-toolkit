package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

internal class ValueWrapperGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    fun getValueWrapper(): FileSpec {
        val className = generatedMapper.getValueWrapperName()

        // Create the Type Variable with Any as bound.
        val parameterType = TypeVariableName("T", ClassName("kotlin", "Any").copy(true))

        return getFileSpecBuilder(className)
            .addType(
                TypeSpec
                    .classBuilder(className)
                    .addTypeVariable(parameterType)
                    // Create the primary constructor with a "value" parameter.
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("value", parameterType)
                            .build()
                    )
                    // Create the "value" property which will be initialized by the previously created primary constructor.
                    .addProperty(
                        PropertySpec.builder("value", parameterType)
                            .initializer("value")
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
