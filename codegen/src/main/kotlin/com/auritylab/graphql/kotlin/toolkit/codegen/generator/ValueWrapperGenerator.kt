package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenInternalOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.*

internal class ValueWrapperGenerator(
        options: CodegenInternalOptions, kotlinTypeMapper: KotlinTypeMapper, private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    fun getValueWrapper(): FileSpec {
        val className = generatedMapper.getValueWrapperName()

        // Create the Type Variable with Any as bound.
        val parameterType = TypeVariableName("T", ClassName("kotlin", "Any").copy(true))

        return getFileSpecBuilder(className)
                .addType(TypeSpec
                        .classBuilder(className)
                        .addTypeVariable(parameterType)
                        // Create the primary constructor with a "value" parameter.
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("value", parameterType)
                                .build())
                        // Create the "value" property which will be initialized by the previously created primary constructor.
                        .addProperty(PropertySpec.builder("value", parameterType)
                                .initializer("value")
                                .build())
                        .build())
                .build()
    }
}
