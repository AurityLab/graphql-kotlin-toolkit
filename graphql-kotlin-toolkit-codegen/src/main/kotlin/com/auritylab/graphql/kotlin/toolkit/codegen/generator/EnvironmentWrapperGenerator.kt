package com.auritylab.graphql.kotlin.toolkit.codegen.generator

import com.auritylab.graphql.kotlin.toolkit.codegen.CodegenOptions
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.GeneratedMapper
import com.auritylab.graphql.kotlin.toolkit.codegen.mapper.KotlinTypeMapper
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

internal class EnvironmentWrapperGenerator(
    options: CodegenOptions,
    kotlinTypeMapper: KotlinTypeMapper,
    private val generatedMapper: GeneratedMapper
) : AbstractGenerator(options, kotlinTypeMapper, generatedMapper) {
    companion object {
        private val dataFetchingEnvironmentClassName = ClassName("graphql.schema", "DataFetchingEnvironment")
    }

    fun getEnvironmentWrapper(): FileSpec {
        val className = generatedMapper.getEnvironmentWrapperClassName()

        val typeVariableName = TypeVariableName("T", ANY)

        return getFileSpecBuilder(className)
            .addType(
                TypeSpec.classBuilder(className)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("original", dataFetchingEnvironmentClassName)
                            .addTypeVariable(typeVariableName)
                            .build()
                    )
                    .addTypeVariable(typeVariableName)
                    .addSuperinterface(dataFetchingEnvironmentClassName, "original")
                    .addProperty(
                        PropertySpec.builder("parent", typeVariableName)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return getSource()")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
